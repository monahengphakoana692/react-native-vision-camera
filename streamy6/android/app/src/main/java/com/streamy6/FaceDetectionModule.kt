package com.streamy6

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.mediapipe.tasks.vision.core.RunningMode

class FaceDetectionModule(
    reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {
    
    private var faceDetectorHelper: FaceDetectorHelper? = null
    
    override fun getName(): String = "FaceDetection"
    
    @ReactMethod
    fun initialize(threshold: Double, delegate: String, promise: Promise) {
        try {
            val context = reactApplicationContext.applicationContext
            val delegateInt = when (delegate.uppercase()) {
                "GPU" -> FaceDetectorHelper.DELEGATE_GPU
                else -> FaceDetectorHelper.DELEGATE_CPU
            }
            
            faceDetectorHelper = FaceDetectorHelper(
                threshold = threshold.toFloat(),
                currentDelegate = delegateInt,
                runningMode = RunningMode.LIVE_STREAM,
                context = context,
                faceDetectorListener = object : FaceDetectorHelper.DetectorListener {
                    override fun onError(error: String, errorCode: Int) {
                        val errorMap = Arguments.createMap()
                        errorMap.putString("error", error)
                        errorMap.putInt("code", errorCode)
                        sendEvent("onFaceDetectionError", errorMap)
                    }
                    
                    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
                        val resultMap = convertResultBundleToMap(resultBundle)
                        sendEvent("onFaceDetected", resultMap)
                    }
                }
            )
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", e.message, e)
        }
    }
    
    @ReactMethod
    fun detectFromBitmap(bitmap: ReadableMap, promise: Promise) {
        promise.reject("NOT_IMPLEMENTED", "Bitmap detection not implemented yet")
    }
    
    @ReactMethod
    fun cleanup() {
        faceDetectorHelper?.clearFaceDetector()
    }
    
    @ReactMethod
    fun addListener(eventName: String) {
        // Required for RN built-in Event Emitter Calls in React Native 0.65+
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Required for RN built-in Event Emitter Calls in React Native 0.65+
    }
    
    // ======================
    // ADD THIS MISSING METHOD
    // ======================
    
    private fun convertResultBundleToMap(resultBundle: FaceDetectorHelper.ResultBundle): WritableMap {
        val resultMap = Arguments.createMap()
        
        // Basic metadata
        resultMap.putInt("imageWidth", resultBundle.inputImageWidth)
        resultMap.putInt("imageHeight", resultBundle.inputImageHeight)
        resultMap.putDouble("inferenceTime", resultBundle.inferenceTime.toDouble())
        
        // Face detection results
        val facesArray = Arguments.createArray()
        
        resultBundle.results.forEach { faceResult ->
            val faceMap = Arguments.createMap()
            
            // Convert each detection
            val detectionsArray = Arguments.createArray()
            faceResult.detections().forEach { detection ->
                val detectionMap = Arguments.createMap()
                
                 // Bounding box - coordinates are Float, not Int
                val box = detection.boundingBox()
                val boxMap = Arguments.createMap()
                boxMap.putDouble("left", box.left.toDouble())    // Convert Float to Double
                boxMap.putDouble("top", box.top.toDouble())      // Convert Float to Double
                boxMap.putDouble("right", box.right.toDouble())  // Convert Float to Double
                boxMap.putDouble("bottom", box.bottom.toDouble())// Convert Float to Double
                boxMap.putDouble("width", box.width().toDouble()) // Convert Float to Double
                boxMap.putDouble("height", box.height().toDouble()) // Convert Float to Double
                detectionMap.putMap("boundingBox", boxMap)
                
                // Categories (scores)
                val categoriesArray = Arguments.createArray()
                detection.categories().forEach { category ->
                    val categoryMap = Arguments.createMap()
                    categoryMap.putString("categoryName", category.categoryName())
                    categoryMap.putDouble("score", category.score().toDouble())
                    categoriesArray.pushMap(categoryMap)
                }
                detectionMap.putArray("categories", categoriesArray)
                
                detectionsArray.pushMap(detectionMap)
            }
            
            faceMap.putArray("detections", detectionsArray)
            facesArray.pushMap(faceMap)
        }
        
        resultMap.putArray("faces", facesArray)
        return resultMap
    }
    
    private fun sendEvent(eventName: String, data: Any?) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, data)
    }
}