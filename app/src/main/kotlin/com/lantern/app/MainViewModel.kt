package com.lantern.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lantern.app.server.FileServer
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

class MainViewModel : ViewModel() {
    var isServerRunning by mutableStateOf(false)
        private set

    var ipAddress by mutableStateOf(getDeviceIpAddress())
        private set
        
    var port by mutableStateOf(8080)
        private set

    private val fileServer = FileServer()

    fun toggleServer(rootDir: File) {
        if (isServerRunning) {
            stopServer()
        } else {
            startServer(rootDir)
        }
    }

    private fun startServer(rootDir: File) {
        try {
            if (!rootDir.exists()) rootDir.mkdirs()
            val welcomeFile = File(rootDir, "Welcome.txt")
            if (!welcomeFile.exists()) {
                welcomeFile.writeText("Welcome to Lantern! Share your files here.")
            }
            
            fileServer.start(port, rootDir)
            isServerRunning = true
            ipAddress = getDeviceIpAddress()
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, update an error state here
        }
    }

    private fun stopServer() {
        fileServer.stop()
        isServerRunning = false
    }
    
    fun refreshIp() {
        ipAddress = getDeviceIpAddress()
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
    
    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}