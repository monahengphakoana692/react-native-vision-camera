package com.streamy6.streaming

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

class VideoEncoder(
    private val width: Int,
    private val height: Int
) {
    private val TAG = "VideoEncoder"
    private var codec: MediaCodec? = null
    private var isRunning = false
    private var frameCount = 0

    fun start() {
        try {
            val safeWidth = (width + 1) and 1.inv()
            val safeHeight = (height + 1) and 1.inv()
            
            Log.d(TAG, "📐 Starting encoder with dimensions: ${safeWidth}x$safeHeight")

            val format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                safeWidth,
                safeHeight
            )
            
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            )
            
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1_000_000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            
            Log.d(TAG, "🔧 Creating MediaCodec...")
            
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec?.start()
            
            isRunning = true
            Log.d(TAG, "✅ MediaCodec started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start encoder: ${e.message}")
            e.printStackTrace()
            stop()
        }
    }

    fun encode(nv21: ByteArray) {
        if (!isRunning || codec == null) {
            Log.w(TAG, "⚠️ Encoder not running, skipping frame")
            return
        }

        frameCount++
        
        if (frameCount == 1 || frameCount % 30 == 0) {
            Log.d(TAG, "🔧 Encoding frame $frameCount: ${nv21.size} bytes")
        }

        try {
            val inputIndex = codec?.dequeueInputBuffer(10000) ?: -1
            if (inputIndex < 0) {
                if (frameCount == 1) {
                    Log.w(TAG, "⚠️ No input buffer available")
                }
                return
            }

            val inputBuffer = codec?.getInputBuffer(inputIndex)
            if (inputBuffer == null) {
                Log.w(TAG, "⚠️ Could not get input buffer")
                return
            }
            
            inputBuffer.clear()
            
            if (nv21.size > inputBuffer.remaining()) {
                Log.w(TAG, "Input too large: ${nv21.size} > ${inputBuffer.remaining()}")
                return
            }
            
            inputBuffer.put(nv21)

            codec?.queueInputBuffer(
                inputIndex,
                0,
                nv21.size,
                System.nanoTime() / 1000,
                0
            )
            
            if (frameCount == 1) {
                Log.d(TAG, "✅ First frame queued for encoding")
            }
            
            drain()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Encode error: ${e.message}")
        }
    }

    private fun drain() {
        if (codec == null) return
        
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            val outputIndex = codec?.dequeueOutputBuffer(bufferInfo, 0) ?: MediaCodec.INFO_TRY_AGAIN_LATER

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> break
                
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    val format = codec?.outputFormat
                    Log.d(TAG, "🎯 Output format changed: $format")
                }
                
                outputIndex >= 0 -> {
                    val outBuffer = codec?.getOutputBuffer(outputIndex)
                    if (outBuffer != null && bufferInfo.size > 0) {
                        // Just log that we got encoded data
                        if (frameCount % 30 == 0) {
                            Log.d(TAG, "🎥 Frame $frameCount: ${bufferInfo.size} bytes encoded")
                        }
                        // TODO: Send to RTMP here
                    }
                    codec?.releaseOutputBuffer(outputIndex, false)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        
        try {
            codec?.stop()
            codec?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping codec: ${e.message}")
        }
        
        codec = null
        Log.d(TAG, "🛑 Encoder stopped. Total frames: $frameCount")
    }
}