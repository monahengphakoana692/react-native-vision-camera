package com.streamy6.encoder

import android.media.*
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class VideoEncoder(
    private val width: Int,
    private val height: Int,
    private val bitrate: Int = 2_000_000, // Reduced from 4M to 2M
    private val fps: Int = 30,
    private val iFrameInterval: Int = 2
) {

    companion object {
        private const val TAG = "VideoEncoder"
        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    }

    private var codec: MediaCodec? = null
    private var inputSurface: Surface? = null
    private val bufferInfo = MediaCodec.BufferInfo()
    private val running = AtomicBoolean(false)

    fun start(): Surface {
        try {
            // First, let's find a supported codec
            val codecName = findAvcEncoder() ?: throw IllegalStateException("No AVC encoder found")
            
            val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
                
                // Don't set profile/level - let the codec choose based on capabilities
                // Or use these safer values:
                setInteger(MediaFormat.KEY_PROFILE, 
                    MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
                setInteger(MediaFormat.KEY_LEVEL, 
                    MediaCodecInfo.CodecProfileLevel.AVCLevel31) // For 720p@30fps
            }

            Log.d(TAG, "Creating encoder: $codecName with format: $format")
            
            codec = MediaCodec.createByCodecName(codecName)
            codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            inputSurface = codec!!.createInputSurface()
            codec!!.start()
            running.set(true)

            Log.d(TAG, "Encoder started successfully $width x $height")
            return inputSurface!!
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start encoder", e)
            stop()
            throw e
        }
    }

    private fun findAvcEncoder(): String? {
        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (info in mediaCodecList.codecInfos) {
            if (info.isEncoder) {
                for (mimeType in info.supportedTypes) {
                    if (mimeType.equals(MIME_TYPE, ignoreCase = true)) {
                        Log.d(TAG, "Found encoder: ${info.name}")
                        return info.name
                    }
                }
            }
        }
        return null
    }

    fun drain(onFrame: (ByteBuffer, MediaCodec.BufferInfo) -> Unit) {
        if (!running.get() || codec == null) return

        try {
            while (true) {
                val index = codec!!.dequeueOutputBuffer(bufferInfo, 0)
                when {
                    index == MediaCodec.INFO_TRY_AGAIN_LATER -> return
                    index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val format = codec!!.outputFormat
                        Log.d(TAG, "Output format changed: $format")
                    }
                    index >= 0 -> {
                        val buf = codec!!.getOutputBuffer(index)
                        if (buf != null && bufferInfo.size > 0) {
                            buf.position(bufferInfo.offset)
                            buf.limit(bufferInfo.offset + bufferInfo.size)
                            onFrame(buf, bufferInfo)
                        }
                        codec!!.releaseOutputBuffer(index, false)
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Codec is in wrong state", e)
        }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        try {
            codec?.stop()
            codec?.release()
            inputSurface?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping encoder", e)
        } finally {
            codec = null
            inputSurface = null
        }
    }
}