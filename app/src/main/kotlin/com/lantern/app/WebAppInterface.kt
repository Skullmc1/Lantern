package com.lantern.app

import android.webkit.JavascriptInterface
import org.json.JSONObject
import com.google.gson.Gson

class WebAppInterface(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) {
    private val gson = Gson()

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
        // We can inject pending sessions here or make a separate call
        // For polling simplicity, let's just make a separate call or add it here?
        // Separate call is better for modularity.
        return json.toString()
    }
    
    @JavascriptInterface
    fun getPendingSessions(): String {
        // This is a bit hacky because it's a synchronous JS call to a StateFlow.
        // But the value is in memory.
        val sessions = viewModel.pendingSessions.value
        return gson.toJson(sessions)
    }
    
    @JavascriptInterface
    fun approveSession(sessionId: String) {
        viewModel.approveSession(sessionId)
    }
    
    @JavascriptInterface
    fun rejectSession(sessionId: String) {
        viewModel.rejectSession(sessionId)
    }
    
    @JavascriptInterface
    fun createShare(path: String): String {
        // Returns JSON of SharedItem or empty/null
        val item = viewModel.createShareFromPath(path)
        return gson.toJson(item)
    }
    
    @JavascriptInterface
    fun getShares(): String {
        return gson.toJson(viewModel.getShares())
    }
    
    @JavascriptInterface
    fun revokeShare(token: String) {
        viewModel.revokeShare(token)
    }

    @JavascriptInterface
    fun pickFile() {
        activity.runOnUiThread {
            activity.openFilePicker()
        }
    }

    @JavascriptInterface
    fun pickFolder() {
        activity.runOnUiThread {
            activity.openFolderPicker()
        }
    }
}
