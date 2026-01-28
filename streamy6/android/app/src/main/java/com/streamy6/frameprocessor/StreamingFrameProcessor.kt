package com.streamy6.frameprocessor

import android.graphics.ImageFormat
import android.util.Log
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.streamy6.streaming.FrameSink
import java.nio.ByteBuffer

class StreamingFrameProcessor : FrameProcessorPlugin() {
    private val TAG = "StreamingFrameProcessor"
    private var frameCount = 0

    override fun callback(frame: Frame, params: Map<String, Any>?): Any? {
        frameCount++
        
        try {
            val image = frame.image ?: return null
            
            if (image.format != ImageFormat.YUV_420_888) {
                Log.w(TAG, "⚠️ Unsupported format: ${image.format}")
                return null
            }

            val width = image.width
            val height = image.height
            
            // Log first frame and then every 30 frames
            if (frameCount == 1 || frameCount % 30 == 0) {
                Log.d(TAG, "📸 Frame $frameCount: ${width}x$height")
            }

            val planes = image.planes
            val yuvSize = width * height * 3 / 2
            val i420 = ByteArray(yuvSize)

            // Extract YUV planes
            var offset = 0
            offset += copyPlane(planes[0], width, height, i420, offset) // Y
            offset += copyPlane(planes[1], width / 2, height / 2, i420, offset) // U
            copyPlane(planes[2], width / 2, height / 2, i420, offset) // V

            // Send to encoder
            FrameSink.onFrame(i420, width, height)
            
            if (frameCount == 1) {
                Log.d(TAG, "✅ First frame sent to FrameSink: ${width}x$height")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing frame: ${e.message}")
        }

        return null
    }

    private fun copyPlane(
        plane: android.media.Image.Plane,
        width: Int,
        height: Int,
        out: ByteArray,
        offset: Int
    ): Int {
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        var outOffset = offset
        
        if (pixelStride == 1 && rowStride == width) {
            // Fast path: contiguous data
            buffer.rewind()
            buffer.get(out, offset, width * height)
            return width * height
        }

        // Slow path: need to skip padding
        val row = ByteArray(rowStride)
        for (y in 0 until height) {
            buffer.get(row, 0, rowStride)
            var col = 0
            for (x in 0 until width) {
                out[outOffset++] = row[col]
                col += pixelStride
            }
        }
        return width * height
    }
}