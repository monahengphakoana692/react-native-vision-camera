package com.streamy6.rtmp

import android.media.MediaCodec
import java.nio.ByteBuffer

class RtmpStreamer(private val url: String) {

    fun sendVideo(buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        // TODO:
        // - prepend SPS/PPS on keyframes
        // - write RTMP packet
        // This is identical to Ant Media / librtmp flow
    }

    fun stop() {}
}
