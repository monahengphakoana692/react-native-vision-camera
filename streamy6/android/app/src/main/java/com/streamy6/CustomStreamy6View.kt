package com.streamy6

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

class CustomStreamy6View(
    private val reactContext: ThemedReactContext
) : ConstraintLayout(reactContext),
    FaceDetectorHelper.DetectorListener,
    LifecycleEventListener {

    // -----------------------
    // STATE
    // -----------------------
    @Volatile private var detectorReady = false
    private var cameraStarted = false
    private var isEnabled = false
    private var showDetection = false

    // -----------------------
    // UI
    // -----------------------
    private val previewView = PreviewView(context)
    private val overlayView = OverlayView(context)

    // -----------------------
    // CAMERA
    // -----------------------
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null

    // -----------------------
    // DETECTION
    // -----------------------
    private lateinit var faceDetectorHelper: FaceDetectorHelper
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

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
    // PUBLIC API (REACT PROPS)
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
        cameraProvider?.unbindAll()
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

        preview = Preview.Builder()
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
                        if (::faceDetectorHelper.isInitialized) {
                            faceDetectorHelper.detectLivestreamFrame(image)
                        } else image.close()
                    }
                }
        }

        provider.unbindAll()
        provider.bindToLifecycle(
            reactContext.currentActivity as AppCompatActivity,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            *(listOfNotNull(preview, imageAnalyzer).toTypedArray())
        )
    }

    // ======================================================
    // FACE DETECTOR
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
    // REACT LIFECYCLE
    // ======================================================
    override fun onHostResume() {
        if (isEnabled) startCamera()
    }

    override fun onHostPause() {
        stopCamera()
    }

    override fun onHostDestroy() {
        stopCamera()
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

        fun setResults(
            r: FaceDetectorResult?,
            h: Int,
            w: Int
        ) {
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
