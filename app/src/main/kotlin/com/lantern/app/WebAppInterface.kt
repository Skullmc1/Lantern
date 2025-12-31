package com.lantern.app

import android.webkit.JavascriptInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class WebAppInterface(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) {
    @JavascriptInterface
    fun toggleServer() {
        // Needs to run on main thread if touching UI, or background if purely logic. 
        // ViewModel operations are usually safe but let's be careful.
        // Actually we need to check permissions on the activity side, 
        // but if permissions are granted, we can toggle.
        
        GlobalScope.launch(Dispatchers.Main) {
            val rootDir = activity.getStorageRoot()
            if (rootDir != null) {
                viewModel.toggleServer(rootDir)
            }
        }
    }

    @JavascriptInterface
    fun checkPermission(): Boolean {
        return activity.checkStoragePermission()
    }

    @JavascriptInterface
    fun requestPermission() {
        activity.requestStoragePermission()
    }

    @JavascriptInterface
    fun getServerStatus(): String {
        val json = JSONObject()
        json.put("isRunning", viewModel.isServerRunning)
        json.put("ip", viewModel.ipAddress)
        json.put("port", viewModel.port)
        return json.toString()
    }
}
