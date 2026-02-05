package com.streamy6.gl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface
import com.streamy6.encoder.VideoEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraEglRenderer(
    private val encoder: VideoEncoder
) {

    companion object {
        private const val TAG = "CameraEglRenderer"
        
        // Vertex shader for rendering camera texture
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = aPosition;
                vTextureCoord = aTextureCoord;
            }
        """
        
        // Fragment shader for external OES texture (camera)
        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;
            void main() {
                gl_FragColor = texture2D(sTexture, vTextureCoord);
            }
        """
        
        // Full screen quad vertices (x, y)
        private val VERTEX_COORDS = floatArrayOf(
            -1.0f, -1.0f,  // bottom left
             1.0f, -1.0f,  // bottom right
            -1.0f,  1.0f,  // top left
             1.0f,  1.0f   // top right
        )
        
        // Texture coordinates (u, v) - flipped for camera
        private val TEXTURE_COORDS = floatArrayOf(
            0.0f, 1.0f,  // bottom left
            1.0f, 1.0f,  // bottom right
            0.0f, 0.0f,  // top left
            1.0f, 0.0f   // top right
        )
    }

    private val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface
    private var surfaceTexture: SurfaceTexture? = null
    private var textureId = 0
    
    // Shader program variables
    private var program = 0
    private var positionHandle = 0
    private var textureCoordHandle = 0
    private var textureHandle = 0
    
    // Buffers
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null
    
    // Debug variables
    private var frameCount = 0L
    private var lastLogTime = 0L

    fun start(): SurfaceTexture {
        Log.d(TAG, "Starting EGL renderer")
        
        try {
            // Initialize EGL
            initEGL()
            
            // Initialize OpenGL and compile shaders
            initOpenGL()
            
            // Create SurfaceTexture for camera
            createSurfaceTexture()
            
            Log.d(TAG, "CameraEglRenderer started successfully")
            return surfaceTexture!!
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start CameraEglRenderer", e)
            release()
            throw e
        }
    }

    private fun initEGL() {
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("Unable to initialize EGL14")
        }

        // Configure EGL
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

        // Create EGL context
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

        // Get encoder surface
        val encoderSurface = encoder.start()

        // Create EGL surface
        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            eglConfig,
            encoderSurface,
            intArrayOf(EGL14.EGL_NONE),
            0
        ) ?: throw RuntimeException("Failed to create EGL surface")

        // Make current
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }

        Log.d(TAG, "EGL initialized successfully")
    }

    private fun initOpenGL() {
        // Compile and link shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        
        // Create program
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // Check link status
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetProgramInfoLog(program)
            throw RuntimeException("Failed to link program: $error")
        }
        
        // Get attribute/uniform locations
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        textureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        
        // Check if we got valid handles
        if (positionHandle == -1 || textureCoordHandle == -1 || textureHandle == -1) {
            throw RuntimeException("Failed to get shader attribute/uniform locations")
        }
        
        // Create vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COORDS.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(VERTEX_COORDS)
                position(0)
            }
        
        // Create texture coordinate buffer
        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(TEXTURE_COORDS)
                position(0)
            }
        
        Log.d(TAG, "OpenGL program created: $program")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        // Check compilation status
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compilation failed: $error")
        }
        
        return shader
    }

    private fun createSurfaceTexture() {
        // Generate texture
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        // Bind as external OES texture
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        
        // Set texture parameters
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
        
        // Create SurfaceTexture
        surfaceTexture = SurfaceTexture(textureId)
        
        // Set default buffer size
        surfaceTexture!!.setDefaultBufferSize(640, 480)
        
        // Set frame available listener
        surfaceTexture!!.setOnFrameAvailableListener { st ->
            drawFrame(st)
        }
        
        Log.d(TAG, "SurfaceTexture created with texture ID: $textureId")
    }

    fun drawFrame(surfaceTexture: SurfaceTexture) {
        try {
            // Update texture with new camera frame
            surfaceTexture.updateTexImage()
            
            // Debug logging
            frameCount++
            val now = System.currentTimeMillis()
            if (now - lastLogTime > 1000) {
                Log.d(TAG, "Drawing frame $frameCount")
                lastLogTime = now
            }
            
            // Clear to black
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            
            // Use our shader program
            GLES20.glUseProgram(program)
            
            // Set vertex position attribute
            GLES20.glEnableVertexAttribArray(positionHandle)
            vertexBuffer?.let {
                GLES20.glVertexAttribPointer(
                    positionHandle, 2, GLES20.GL_FLOAT, false, 0, it
                )
            }
            
            // Set texture coordinate attribute
            GLES20.glEnableVertexAttribArray(textureCoordHandle)
            textureBuffer?.let {
                GLES20.glVertexAttribPointer(
                    textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, it
                )
            }
            
            // Bind texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES20.glUniform1i(textureHandle, 0)
            
            // Draw the quad
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            
            // Clean up
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(textureCoordHandle)
            
            // Check for errors
            val error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                Log.e(TAG, "OpenGL error: $error")
            }
            
            // Swap buffers (send to encoder)
            if (!EGL14.eglSwapBuffers(eglDisplay, eglSurface)) {
                val eglError = EGL14.eglGetError()
                Log.e(TAG, "eglSwapBuffers failed: $eglError")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in drawFrame", e)
        }
    }

    fun getSurfaceTexture(): SurfaceTexture? = surfaceTexture

    fun release() {
        Log.d(TAG, "Releasing CameraEglRenderer")
        
        try {
            // Release SurfaceTexture
            surfaceTexture?.release()
            surfaceTexture = null
            
            // Delete OpenGL texture
            if (textureId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
                textureId = 0
            }
            
            // Delete shader program
            if (program != 0) {
                GLES20.glDeleteProgram(program)
                program = 0
            }
            
            // Release EGL resources
            if (::eglSurface.isInitialized) {
                EGL14.eglMakeCurrent(
                    eglDisplay,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                EGL14.eglDestroySurface(eglDisplay, eglSurface)
            }
            
            if (::eglContext.isInitialized) {
                EGL14.eglDestroyContext(eglDisplay, eglContext)
            }
            
            EGL14.eglTerminate(eglDisplay)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources", e)
        }
        
        Log.d(TAG, "CameraEglRenderer released")
    }
    
    fun isInitialized(): Boolean {
        return ::eglSurface.isInitialized && surfaceTexture != null && program != 0
    }
}