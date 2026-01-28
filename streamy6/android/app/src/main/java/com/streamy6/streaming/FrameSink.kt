package com.streamy6.streaming

import android.util.Log

object FrameSink {
    private const val TAG = "FrameSink"
    private var encoder: VideoEncoder? = null
    private var frameCount = 0

    fun start(width: Int, height: Int) {
        try {
            Log.d(TAG, "🚀 Starting VideoEncoder: ${width}x$height")
            encoder = VideoEncoder(width, height)
            encoder?.start()
            Log.d(TAG, "✅ VideoEncoder started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start encoder: ${e.message}")
        }
    }

    fun onFrame(i420: ByteArray, width: Int, height: Int) {
        frameCount++
        
        if (frameCount == 1 || frameCount % 30 == 0) {
            Log.d(TAG, "📦 Received frame $frameCount: ${width}x$height, size: ${i420.size} bytes")
        }
        
        if (encoder == null) {
            start(width, height)
        }

        try {
            val nv21 = i420ToNv21(i420, width, height)
            encoder?.encode(nv21)
            
            if (frameCount == 1) {
                Log.d(TAG, "✅ First frame encoded")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error encoding frame: ${e.message}")
        }
    }

    private fun i420ToNv21(
        i420: ByteArray,
        width: Int,
        height: Int
    ): ByteArray {
        val frameSize = width * height
        val nv21 = ByteArray(frameSize * 3 / 2)

        // Copy Y plane (same in both formats)
        System.arraycopy(i420, 0, nv21, 0, frameSize)

        // Convert UV planes (I420: Y + U + V → NV21: Y + VU)
        val uOffset = frameSize
        val vOffset = frameSize + frameSize / 4
        
        var uvIndex = frameSize
        for (i in 0 until frameSize / 4) {
            nv21[uvIndex++] = i420[vOffset + i]  // V
            nv21[uvIndex++] = i420[uOffset + i]  // U
        }

        return nv21
    }

    fun stop() {
        encoder?.stop()
        encoder = null
        Log.d(TAG, "🛑 FrameSink stopped. Total frames processed: $frameCount")
    }
}