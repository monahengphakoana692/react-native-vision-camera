package com.streamy6.gl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface
import com.streamy6.encoder.VideoEncoder

class CameraEglRenderer(
    private val encoder: VideoEncoder
) {

    companion object {
        private const val TAG = "CameraEglRenderer"
    }

    private val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface
    private var encoderSurface: Surface? = null

    fun start(): SurfaceTexture {
        Log.d(TAG, "Starting EGL renderer")
        
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("Unable to initialize EGL14")
        }

        val config = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        
        if (!EGL14.eglChooseConfig(eglDisplay, config, 0, configs, 0, 1, numConfigs, 0)) {
            throw RuntimeException("eglChooseConfig failed")
        }

        val eglConfig = configs[0] ?: throw RuntimeException("No EGL config found")

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )

        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL14.EGL_NO_CONTEXT,
            contextAttribs,
            0
        ) ?: throw RuntimeException("Failed to create EGL context")

        // Get the encoder surface
        encoderSurface = encoder.start()

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )

        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            eglConfig,
            encoderSurface,
            surfaceAttribs,
            0
        ) ?: throw RuntimeException("Failed to create EGL surface")

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }

        Log.d(TAG, "EGL initialized successfully")
        
        // Create OES texture for camera
        val texId = createOesTexture()
        val surfaceTexture = SurfaceTexture(texId)
        
        // Set the surface texture to use the encoder surface
        surfaceTexture.setDefaultBufferSize(1280, 720)
        
        return surfaceTexture
    }

    fun drawFrame(surfaceTexture: SurfaceTexture) {
        surfaceTexture.updateTexImage()
        
        // Clear to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // Draw the texture (you may need a proper shader here)
        // For now, just do a simple draw
        
        if (!EGL14.eglSwapBuffers(eglDisplay, eglSurface)) {
            Log.e(TAG, "eglSwapBuffers failed: ${EGL14.eglGetError()}")
        }
    }

    private fun createOesTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        
        return tex[0]
    }

    fun release() {
        EGL14.eglMakeCurrent(
            eglDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
        
        encoderSurface?.release()
    }
}