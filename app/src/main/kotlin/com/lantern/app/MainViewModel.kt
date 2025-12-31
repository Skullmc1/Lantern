package com.lantern.app

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantern.app.server.ClientSession
import com.lantern.app.server.SharedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

class MainViewModel : ViewModel() {
    private var lanternService: LanternService? = null
    
    var isServerRunning by mutableStateOf(false)
        private set

    var ipAddress by mutableStateOf("Loading...")
        private set
        
    var port by mutableStateOf(8080)
        private set
        
    private val _pendingSessions = MutableStateFlow<List<ClientSession>>(emptyList())
    val pendingSessions: StateFlow<List<ClientSession>> = _pendingSessions.asStateFlow()

    private var serverRoot: File? = null
    private var serviceJob: Job? = null

    init {
        refreshIp()
    }

    fun bindService(service: LanternService) {
        this.lanternService = service
        isServerRunning = service.isRunning
        
        serviceJob?.cancel()
        serviceJob = viewModelScope.launch {
            service.sessionManager.pendingSessions.collect {
                _pendingSessions.value = it
            }
        }
    }

    fun unbindService() {
        lanternService = null
        serviceJob?.cancel()
    }

    fun toggleServer(rootDir: File) {
        serverRoot = rootDir
        val service = lanternService ?: return
        
        if (service.isRunning) {
            service.stopServer()
            isServerRunning = false
        } else {
            service.startServer(port, rootDir)
            isServerRunning = true
            refreshIp()
        }
    }
    
    fun approveSession(sessionId: String) {
        lanternService?.sessionManager?.approveSession(sessionId)
    }
    
    fun rejectSession(sessionId: String) {
        lanternService?.sessionManager?.rejectSession(sessionId)
    }

    fun createShareFromUri(uri: Uri, contentResolver: ContentResolver) {
        val service = lanternService ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var name = "Unknown"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }

                val cacheDir = File(getApplicationCacheDir(), "shared_items")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val targetFile = File(cacheDir, "${System.currentTimeMillis()}_$name")
                contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                if (targetFile.isFile) {
                    service.sharedItemManager.createShare(targetFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createShareFromPath(relativePath: String): SharedItem? {
        val root = serverRoot ?: return null
        val service = lanternService ?: return null
        val file = File(root, relativePath)
        if (file.exists() && file.isFile) {
            return service.sharedItemManager.createShare(file)
        }
        return null
    }
    
    fun getShares(): List<SharedItem> {
        return lanternService?.sharedItemManager?.getAllShares() ?: emptyList()
    }
    
    fun revokeShare(token: String) {
        lanternService?.sharedItemManager?.revokeShare(token)
    }

    private fun getApplicationCacheDir(): File {
        return serverRoot ?: File("/sdcard/Lantern/cache")
    }

    fun refreshIp() {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = getDeviceIpAddress()
            withContext(Dispatchers.Main) {
                ipAddress = ip
            }
        }
    }

    private fun getDeviceIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Not Connected"
    }
}