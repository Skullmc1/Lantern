package com.lantern.app

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
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

class MainViewModel(application: Application) : AndroidViewModel(application) {
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
            // Update UI instantly, then stop the server in the background.
            // fileServer.stop() blocks (up to ~3s) while Ktor shuts down, so it
            // must not run on the WebView bridge thread or the UI will look stuck.
            isServerRunning = false
            viewModelScope.launch(Dispatchers.IO) {
                service.stopServer()
            }
        } else {
            // Optimistic: show the orb lit immediately while the server starts.
            isServerRunning = true
            service.startServer(port, rootDir)
            refreshIp()
        }
    }
    
    fun approveSession(sessionId: String) {
        lanternService?.sessionManager?.approveSession(sessionId)
    }
    
    fun rejectSession(sessionId: String) {
        lanternService?.sessionManager?.rejectSession(sessionId)
    }

    fun createShareFromUri(uri: Uri) {
        val service = lanternService ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = File(getApplication<Application>().cacheDir, "shared_items")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val target = copyUriToCache(uri, cacheDir)
                if (target != null) {
                    service.sharedItemManager.createShare(target)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun copyUriToCache(uri: Uri, cacheDir: File): File? {
        val resolver = getApplication<Application>().contentResolver
        return if (DocumentsContract.isTreeUri(uri)) {
            val name = queryDisplayName(resolver, uri) ?: "folder_${System.currentTimeMillis()}"
            val dest = File(cacheDir, name)
            dest.mkdirs()
            copyTreeChildren(resolver, uri, dest)
            if (dest.isDirectory) dest else null
        } else {
            val name = queryDisplayName(resolver, uri) ?: "file_${System.currentTimeMillis()}"
            val dest = File(cacheDir, name)
            copyStream(resolver, uri, dest)
            if (dest.isFile) dest else null
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        val columns = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
        resolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
                if (name.isNullOrEmpty()) {
                    val idx2 = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx2 >= 0) name = cursor.getString(idx2)
                }
            }
        }
        return name?.ifEmpty { null } ?: uri.lastPathSegment
    }

    private fun copyTreeChildren(resolver: ContentResolver, treeUri: Uri, destDir: File) {
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocId)
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        resolver.query(childrenUri, columns, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val docId = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                val mime = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                if (DocumentsContract.Document.MIME_TYPE_DIR == mime) {
                    val subDir = File(destDir, name)
                    subDir.mkdirs()
                    copyTreeChildren(resolver, docUri, subDir)
                } else {
                    copyStream(resolver, docUri, File(destDir, name))
                }
            }
        }
    }

    private fun copyStream(resolver: ContentResolver, uri: Uri, target: File) {
        resolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
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