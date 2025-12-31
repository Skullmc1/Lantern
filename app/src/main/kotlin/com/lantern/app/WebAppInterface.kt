package com.lantern.app

import android.webkit.JavascriptInterface
import org.json.JSONObject

class WebAppInterface(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) {
    @JavascriptInterface
    fun toggleServer() {
        val rootDir = activity.getStorageRoot()
        if (rootDir != null) {
            viewModel.toggleServer(rootDir)
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
