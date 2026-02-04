package com.streamy6

import android.util.Log
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class ReactStreamy6Manager : SimpleViewManager<CustomStreamy6View>() {

    override fun getName(): String = "CustomStreamy6"

    override fun createViewInstance(
        reactContext: ThemedReactContext
    ): CustomStreamy6View {
        return CustomStreamy6View(reactContext)
    }

    @ReactProp(name = "enabled", defaultBoolean = false)
    fun setEnabled(view: CustomStreamy6View, enabled: Boolean) {
        Log.d("Streamy6Manager", "Camera enabled = $enabled")
        view.setStreamEnabled(enabled)  // Use the renamed method
    }

   @ReactProp(name = "showDetection", defaultBoolean = true)
    fun setShowDetection(view: CustomStreamy6View, showDetection: Boolean) {  // Fixed parameter name
        Log.d("Streamy6Manager", "Show detection = $showDetection")
        view.setShowDetection(showDetection)  // Use the local parameter
    }
}
