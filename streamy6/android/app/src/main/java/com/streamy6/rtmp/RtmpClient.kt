/*package com.streamy6.rtmp

import android.util.Log
import com.pedro.encoder.input.video.GetCameraData
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.OpenGlView
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.nio.ByteBuffer

class RtmpClient(private val connectListener: ConnectListener) : ConnectCheckerRtmp {
    
    interface ConnectListener {
        fun onConnectionSuccess()
        fun onConnectionFailed(reason: String)
        fun onDisconnected()
        fun onAuthError()
        fun onAuthSuccess()
    }
    
    private var rtmpCamera: RtmpCamera1? = null
    private var isStreaming = false
    private var streamUrl: String? = null
    
    fun initialize(glView: OpenGlView) {
        rtmpCamera = RtmpCamera1(glView, this)
    }
    
    fun startStream(rtmpUrl: String, streamKey: String, width: Int, height: Int, fps: Int, bitrate: Int): Boolean {
        streamUrl = "$rtmpUrl/$streamKey"
        
        return try {
            val camera = rtmpCamera ?: return false
            
            // Configure video
            if (!camera.prepareVideo(width, height, fps, bitrate, 2, 90)) {
                Log.e("RtmpClient", "Failed to prepare video")
                return false
            }
            
            // Configure audio (if needed)
            // camera.prepareAudio()
            
            // Start streaming
            camera.startStream(streamUrl!!)
            isStreaming = true
            true
        } catch (e: Exception) {
            Log.e("RtmpClient", "Failed to start stream", e)
            false
        }
    }
    
    fun stopStream() {
        rtmpCamera?.stopStream()
        isStreaming = false
    }
    
    fun isStreaming(): Boolean = isStreaming
    
    fun release() {
        stopStream()
        rtmpCamera?.release()
        rtmpCamera = null
    }
    
    // ConnectCheckerRtmp callbacks
    override fun onConnectionSuccessRtmp() {
        Log.d("RtmpClient", "RTMP Connection successful")
        connectListener.onConnectionSuccess()
    }
    
    override fun onConnectionFailedRtmp(reason: String) {
        Log.e("RtmpClient", "RTMP Connection failed: $reason")
        connectListener.onConnectionFailed(reason)
    }
    
    override fun onDisconnectRtmp() {
        Log.d("RtmpClient", "RTMP Disconnected")
        connectListener.onDisconnected()
    }
    
    override fun onAuthErrorRtmp() {
        Log.e("RtmpClient", "RTMP Auth error")
        connectListener.onAuthError()
    }
    
    override fun onAuthSuccessRtmp() {
        Log.d("RtmpClient", "RTMP Auth success")
        connectListener.onAuthSuccess()
    }
}*/