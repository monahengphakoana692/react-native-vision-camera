package com.streamy6.rtmp

import android.media.MediaCodec
import android.util.Log
import java.io.*
import java.net.Socket
import java.nio.ByteBuffer

class SimpleRtmpSender(private val serverUrl: String) {
    
    companion object {
        private const val TAG = "SimpleRtmpSender"
        private const val RTMP_PORT = 1935
    }
    
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    
    fun connect(streamKey: String): Boolean {
        return try {
            // Parse URL: rtmp://host[:port]/app
            val url = serverUrl.removePrefix("rtmp://")
            val hostPart = url.substringBefore("/")
            val host = hostPart.substringBefore(":")
            val port = hostPart.substringAfter(":", "1935").toInt()
            val app = url.substringAfter("/", "live")
            
            Log.d(TAG, "Connecting to RTMP server: $host:$port, app: $app, stream: $streamKey")
            
            socket = Socket(host, port)
            outputStream = socket?.getOutputStream()
            
            // Simplified connection - this is where you'd implement RTMP handshake
            // For now, just mark as connected
            isConnected = true
            Log.d(TAG, "Connected to RTMP server (handshake skipped)")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to RTMP server", e)
            disconnect()
            false
        }
    }
    
    fun sendVideoFrame(buffer: ByteBuffer, info: MediaCodec.BufferInfo): Boolean {
        if (!isConnected) return false
        
        try {
            val data = ByteArray(info.size)
            buffer.get(data)
            
            // Log frame info
            when (val nalType = data[0].toInt() and 0x1F) {
                7 -> Log.d(TAG, "SPS frame: ${data.size} bytes")
                8 -> Log.d(TAG, "PPS frame: ${data.size} bytes")
                5 -> Log.d(TAG, "Keyframe: ${data.size} bytes")
                else -> {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "P-frame: ${data.size} bytes")
                    }
                }
            }
            
            // TODO: Implement actual RTMP packet sending
            // For now, just write to output stream (this won't work with Facebook)
            // outputStream?.write(data)
            // outputStream?.flush()
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send video frame", e)
            return false
        }
    }
    
    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        } finally {
            outputStream = null
            socket = null
            isConnected = false
        }
    }
    
    fun isConnected(): Boolean = isConnected
}