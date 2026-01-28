package com.streamy6

import android.app.Application
import android.util.Log
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry
import com.mrousavy.camera.frameprocessors.VisionCameraProxy

class MainApplication : Application(), ReactApplication {
    private val TAG = "MainApplication"

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    // Packages that cannot be autolinked yet can be added manually here
                }

            override fun getJSMainModuleName(): String = "index"
            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 MainApplication.onCreate()")
        loadReactNative(this)

        Log.d(TAG, "🔧 Registering frame processor plugin 'frameLogger'")
        FrameProcessorPluginRegistry.addFrameProcessorPlugin(
            "frameLogger"
        ) { _: VisionCameraProxy, _: Map<String, Any>? ->
            Log.d(TAG, "✅ Creating StreamingFrameProcessor instance")
            com.streamy6.frameprocessor.StreamingFrameProcessor()
        }
    }
}