package com.streamy6.frameprocessor

import android.util.Log
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin

class SimpleFrameProcessor : FrameProcessorPlugin() {
    private val TAG = "SimpleFrameProcessor"
    private var frameCount = 0

    override fun callback(frame: Frame, params: Map<String, Any>?): Any? {
        frameCount++
        
        // Log every frame for testing
        Log.d(TAG, "📸 Frame $frameCount: ${frame.width}x${frame.height}")
        
        return frameCount
    }
}