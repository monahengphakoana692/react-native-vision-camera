package com.streamy6.encoder

import android.media.*
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class VideoEncoder(
    private val width: Int,
    private val height: Int,
    private val bitrate: Int = 2_000_000,
    private val fps: Int = 30,
    private val iFrameInterval: Int = 2
) {

    companion object {
        private const val TAG = "VideoEncoder"
    }

    private var codec: MediaCodec? = null
    private val running = AtomicBoolean(false)
    private val bufferInfo = MediaCodec.BufferInfo()

    fun start(): Surface {
        try {
            Log.d(TAG, "Starting encoder: ${width}x${height}, ${bitrate}bps, ${fps}fps")
            
            // Create format with proper settings
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                
                // Add profile/level for better compatibility
                setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
                setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31)
                
                // Optional: Set bitrate mode
                // setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)
            }
            
            Log.d(TAG, "Encoder format: $format")
            
            // Create encoder
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            
            val surface = codec!!.createInputSurface()
            codec!!.start()
            
            running.set(true)
            
            Log.d(TAG, "Encoder started successfully")
            return surface
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start encoder", e)
            stop()
            throw e
        }
    }

    fun drain(onFrame: (ByteBuffer, MediaCodec.BufferInfo) -> Unit) {
        if (!running.get() || codec == null) {
            Log.v(TAG, "Encoder not running")
            return
        }

        try {
            var hasMore = true
            while (hasMore) {
                val index = codec!!.dequeueOutputBuffer(bufferInfo, 10000)
                
                when (index) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        hasMore = false
                        Log.v(TAG, "No frames available")
                    }
                    
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val format = codec!!.outputFormat
                        Log.d(TAG, "Output format changed: $format")
                    }
                    
                    else -> {
                        if (index >= 0) {
                            val buf = codec!!.getOutputBuffer(index)
                            if (buf != null && bufferInfo.size > 0) {
                                buf.position(bufferInfo.offset)
                                buf.limit(bufferInfo.offset + bufferInfo.size)
                                
                                // Detailed logging
                                val flags = bufferInfo.flags
                                val type = when {
                                    flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0 -> "CONFIG"
                                    flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0 -> "KEYFRAME"
                                    else -> "FRAME"
                                }
                                
                                Log.d(TAG, "Got $type: ${bufferInfo.size} bytes, pts: ${bufferInfo.presentationTimeUs}")
                                
                                onFrame(buf, bufferInfo)
                            }
                            codec!!.releaseOutputBuffer(index, false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error draining encoder", e)
        }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        
        Log.d(TAG, "Stopping encoder")
        
        try {
            codec?.stop()
            codec?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping encoder", e)
        } finally {
            codec = null
        }
    }
    
    fun isRunning(): Boolean = running.get()
}