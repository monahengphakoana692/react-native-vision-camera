package com.streamy6.gl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.Surface
import com.streamy6.encoder.VideoEncoder

class CameraEglRenderer(
    private val encoder: VideoEncoder
) {

    private val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface

    fun start(): SurfaceTexture {
        EGL14.eglInitialize(eglDisplay, null, 0, null, 0)

        val config = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        EGL14.eglChooseConfig(eglDisplay, config, 0, configs, 0, 1, IntArray(1), 0)

        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE),
            0
        )

        val inputSurface = encoder.start()

        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            configs[0],
            inputSurface,
            intArrayOf(EGL14.EGL_NONE),
            0
        )

        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

        val texId = createOesTexture()
        return SurfaceTexture(texId)
    }

    fun drawFrame(surfaceTexture: SurfaceTexture) {
        surfaceTexture.updateTexImage()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    private fun createOesTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        return tex[0]
    }
}
