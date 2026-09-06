package com.lantern.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lantern.app.server.FileServer
import com.lantern.app.server.SessionManager
import com.lantern.app.server.SharedItemManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class LanternService : Service() {

    private val binder = LocalBinder()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Core Managers
    val sessionManager = SessionManager()
    val sharedItemManager = SharedItemManager()
    val fileServer = FileServer(sessionManager, sharedItemManager)

    @Volatile
    var isRunning = false
        private set

    inner class LocalBinder : Binder() {
        fun getService(): LanternService = this@LanternService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopServer()
            stopSelf()
        }
        return START_STICKY
    }

    fun startServer(port: Int, rootDir: File) {
        if (isRunning) return

        startForegroundService()
        
        serviceScope.launch {
            try {
                fileServer.start(port, rootDir)
                isRunning = true
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf() // Stop if failed to start
            }
        }
    }

    /**
     * Stops the file server. May block for a few seconds while Ktor shuts down,
     * so callers that care about UI latency should invoke this from a background
     * thread/coroutine (see MainViewModel.toggleServer). The running flag is
     * cleared immediately so the state is consistent while the stop is in flight.
     */
    fun stopServer() {
        if (!isRunning) return

        isRunning = false
        fileServer.stop()
        stopForeground(true)
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, LanternService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lantern Server Running")
            .setContentText("Your files are being shared locally.")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Ensure this icon exists or use a fallback
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Lantern Server Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
        serviceJob.cancel()
    }

    companion object {
        const val CHANNEL_ID = "LanternServiceChannel"
        const val ACTION_STOP = "STOP_SERVICE"
    }
}
