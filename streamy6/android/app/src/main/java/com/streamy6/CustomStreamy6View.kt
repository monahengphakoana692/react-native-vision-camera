package com.streamy6

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.streamy6.encoder.VideoEncoder
import com.streamy6.gl.CameraEglRenderer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class CustomStreamy6View(
    private val reactContext: ThemedReactContext
) : ConstraintLayout(reactContext),
    FaceDetectorHelper.DetectorListener,
    LifecycleEventListener {

    // ======================================================
    // STATE
    // ======================================================
    @Volatile private var detectorReady = false
    private var cameraStarted = false
    private var isEnabled = false
    private var showDetection = false
    private var streamingStarted = AtomicBoolean(false)

    // ======================================================
    // UI
    // ======================================================
    private val previewView = PreviewView(context)
    private val overlayView = OverlayView(context)

    // ======================================================
    // CAMERA
    // ======================================================
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraDevice: androidx.camera.core.Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null

    // ======================================================
    // DETECTION
    // ======================================================
    private lateinit var faceDetectorHelper: FaceDetectorHelper
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    // ======================================================
    // ENCODER & STREAMING
    // ======================================================
    private var videoEncoder: VideoEncoder? = null
    private var cameraEglRenderer: CameraEglRenderer? = null
    private var encoderDrainThread: Thread? = null

    init {
        reactContext.addLifecycleEventListener(this)
        setupLayout()
        initDetector()
    }

    // ======================================================
    // UI SETUP
    // ======================================================
    private fun setupLayout() {
        setBackgroundColor(Color.BLACK)

        val set = ConstraintSet()
        set.clone(this)

        previewView.id = View.generateViewId()
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        installHierarchyFitter(previewView)
        addView(previewView)

        overlayView.id = View.generateViewId()
        addView(overlayView)

        listOf(previewView.id, overlayView.id).forEach { id ->
            set.constrainWidth(id, ConstraintSet.MATCH_CONSTRAINT)
            set.constrainHeight(id, ConstraintSet.MATCH_CONSTRAINT)
            set.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            set.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }

        set.applyTo(this)
    }

    private fun installHierarchyFitter(view: ViewGroup) {
        view.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View?, child: View?) = Unit
            override fun onChildViewAdded(parent: View?, child: View?) {
                parent?.measure(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                )
                parent?.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
            }
        })
    }

    // ======================================================
    // REACT PROPS
    // ======================================================
    fun setStreamEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) startCamera() else stopCamera()
    }

    fun setShowDetection(show: Boolean) {
        showDetection = show
        if (!show) overlayView.clear()
        if (cameraStarted) bindCamera()
    }

    /**
     * 🔥 CALLED FROM REACT BUTTON
     */
    fun startStreaming() {
        if (streamingStarted.get()) {
            Log.w("Streamy6", "Streaming already started")
            return
        }
        
        if (!cameraStarted) {
            Log.w("Streamy6", "Cannot start streaming – camera not ready")
            return
        }

        Log.w("Streamy6", "Starting streaming pipeline ------------------------------------")
        
        handler.post {
            try {
                startEncoder()
                streamingStarted.set(true)
                Log.d("Streamy6", "Streaming started successfully")
            } catch (e: Exception) {
                Log.e("Streamy6", "Failed to start streaming", e)
                streamingStarted.set(false)
                stopEncoder()
            }
        }
    }

    fun stopStreaming() {
        if (!streamingStarted.get()) return
        
        Log.d("Streamy6", "Stopping streaming")
        streamingStarted.set(false)
        stopEncoder()
    }

    // ======================================================
    // CAMERA
    // ======================================================
    private fun startCamera() {
        if (cameraStarted || !isEnabled) return

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
            bindCamera()
            cameraStarted = true
        }, ContextCompat.getMainExecutor(context))
    }

    private fun stopCamera() {
        stopStreaming()
        cameraProvider?.unbindAll()
        cameraDevice = null
        cameraStarted = false
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    AspectRatio.RATIO_16_9,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageAnalyzer = null
        if (showDetection && detectorReady) {
            imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(executor) { image ->
                        faceDetectorHelper.detectLivestreamFrame(image)
                    }
                }
        }

        provider.unbindAll()
        cameraDevice = provider.bindToLifecycle(
            reactContext.currentActivity as AppCompatActivity,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            *(listOfNotNull(preview, imageAnalyzer).toTypedArray())
        )
    }

    // ======================================================
    // ENCODER & STREAMING PIPELINE
    // ======================================================
    private fun startEncoder() {
        if (videoEncoder != null) {
            Log.w("Streamy6", "Encoder already started")
            return
        }

        try {
            // Create encoder
            videoEncoder = VideoEncoder(
                width = 640,
                height = 480,
                fps = 30,
                bitrate = 1_000_000
            )

            // Create EGL renderer
            cameraEglRenderer = CameraEglRenderer(videoEncoder!!)
            val surfaceTexture = cameraEglRenderer!!.start()
            
            // Connect camera to EGL SurfaceTexture
            connectCameraToEgl(surfaceTexture)
            
            // Start encoder drain thread
            startEncoderDrainThread()
            
            Log.d("Streamy6", "Encoder pipeline started with EGL renderer")
            
        } catch (e: Exception) {
            Log.e("Streamy6", "Failed to start encoder pipeline", e)
            stopEncoder()
            throw e
        }
    }

    private fun connectCameraToEgl(surfaceTexture: android.graphics.SurfaceTexture) {
        val provider = cameraProvider ?: return
        
        Log.d("Streamy6", "Connecting camera to EGL SurfaceTexture")
        
        provider.unbindAll()
        
        // Create Surface from EGL SurfaceTexture
        val surface = Surface(surfaceTexture)
        
        val preview = Preview.Builder()
            .build()
            .also { 
                it.setSurfaceProvider { request ->
                    request.provideSurface(surface, executor) { result ->
                        Log.d("Streamy6", "CameraX surface result: $result")
                    }
                }
            }
        
        provider.bindToLifecycle(
            reactContext.currentActivity as AppCompatActivity,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview
        )
        
        Log.d("Streamy6", "Camera connected to EGL pipeline")
    }

    private fun startEncoderDrainThread() {
        encoderDrainThread = Thread {
            Log.d("Streamy6", "Encoder drain thread started")
            
            while (streamingStarted.get() && videoEncoder != null) {
                try {
                    // Drain encoded frames
                    videoEncoder!!.drain { buffer, bufferInfo ->
                        // Handle encoded frame (send to RTMP/WebRTC/etc.)
                        handleEncodedFrame(buffer, bufferInfo)
                    }
                    
                    // Throttle to avoid busy waiting
                    Thread.sleep(10)
                    
                } catch (e: InterruptedException) {
                    Log.d("Streamy6", "Encoder drain thread interrupted")
                    break
                } catch (e: Exception) {
                    Log.e("Streamy6", "Error in encoder drain thread", e)
                    if (streamingStarted.get()) {
                        // Try to recover
                        Thread.sleep(100)
                    }
                }
            }
            
            Log.d("Streamy6", "Encoder drain thread stopped")
        }
        
        encoderDrainThread?.priority = Thread.MAX_PRIORITY
        encoderDrainThread?.start()
    }

    private fun handleEncodedFrame(buffer: java.nio.ByteBuffer, bufferInfo: android.media.MediaCodec.BufferInfo) {
        // TODO: Implement your streaming logic here
        // This could be RTMP, WebRTC, or saving to file
        
        // For debugging:
        if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            Log.d("Streamy6", "Got codec config data")
        } else if (bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) {
            Log.d("Streamy6", "Got keyframe: ${bufferInfo.size} bytes")
        } else {
            Log.d("Streamy6", "Got frame: ${bufferInfo.size} bytes, pts: ${bufferInfo.presentationTimeUs}")
        }
        
        // Example: Send to RTMP server
        // if (rtmpClient != null && rtmpClient.isConnected()) {
        //     rtmpClient.sendVideoData(buffer, bufferInfo)
        // }
    }

    private fun stopEncoder() {
        streamingStarted.set(false)
        
        // Stop drain thread
        encoderDrainThread?.interrupt()
        encoderDrainThread?.join(1000)
        encoderDrainThread = null
        
        // Release EGL renderer
        cameraEglRenderer?.release()
        cameraEglRenderer = null
        
        // Stop encoder
        videoEncoder?.stop()
        videoEncoder = null
        
        // Rebind camera for preview
        if (cameraStarted) {
            bindCamera()
        }
        
        Log.d("Streamy6", "Encoder stopped")
    }

    // ======================================================
    // DETECTOR
    // ======================================================
    private fun initDetector() {
        executor.execute {
            faceDetectorHelper = FaceDetectorHelper(
                context = reactContext,
                threshold = FaceDetectorHelper.THRESHOLD_DEFAULT,
                currentDelegate = FaceDetectorHelper.DELEGATE_CPU,
                runningMode = RunningMode.LIVE_STREAM,
                faceDetectorListener = this
            )
            detectorReady = true
            Log.d("Streamy6", "FaceDetector ready")
        }
    }

    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
        if (!showDetection) return
        overlayView.setResults(
            resultBundle.results.firstOrNull(),
            resultBundle.inputImageHeight,
            resultBundle.inputImageWidth
        )
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e("Streamy6", error)
    }

    // ======================================================
    // LIFECYCLE
    // ======================================================
    override fun onHostResume() {
        if (isEnabled) startCamera()
    }

    override fun onHostPause() {
        stopCamera()
    }

    override fun onHostDestroy() {
        stopCamera()
        streamingStarted.set(false)
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
    }

    // ======================================================
    // OVERLAY
    // ======================================================
    private class OverlayView(context: Context) : View(context) {
        private var result: FaceDetectorResult? = null
        private var scale = 1f

        private val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        fun setResults(r: FaceDetectorResult?, h: Int, w: Int) {
            result = r
            if (w > 0 && h > 0) {
                scale = min(width.toFloat() / w, height.toFloat() / h)
            }
            postInvalidate()
        }

        fun clear() {
            result = null
            postInvalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            result?.detections()?.forEach {
                val b = it.boundingBox()
                canvas.drawRect(
                    b.left * scale,
                    b.top * scale,
                    b.right * scale,
                    b.bottom * scale,
                    paint
                )
            }
        }
    }
}