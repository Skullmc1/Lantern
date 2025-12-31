package com.lantern.app.server

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class AuthStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class ClientSession(
    val id: String,
    val deviceName: String,
    val ipAddress: String,
    var status: AuthStatus = AuthStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

class SessionManager {
    // Map of SessionID -> ClientSession
    private val sessions = ConcurrentHashMap<String, ClientSession>()
    
    // Observable list of pending requests for the UI
    private val _pendingSessions = MutableStateFlow<List<ClientSession>>(emptyList())
    val pendingSessions = _pendingSessions.asStateFlow()

    fun requestSession(deviceName: String, ipAddress: String): String {
        // Simple check: if this IP already has an approved session, maybe reuse it?
        // For now, strict new session per request for security/simplicity unless cookie exists.
        val sessionId = UUID.randomUUID().toString()
        val session = ClientSession(sessionId, deviceName, ipAddress, AuthStatus.PENDING)
        sessions[sessionId] = session
        updatePendingList()
        return sessionId
    }

    fun getSession(sessionId: String): ClientSession? {
        return sessions[sessionId]
    }
    
    fun getSessionStatus(sessionId: String): AuthStatus {
        val session = sessions[sessionId] ?: return AuthStatus.REJECTED // Or NONE effectively
        
        // Check Expiry (24 hours = 86400000 ms)
        if (System.currentTimeMillis() - session.timestamp > 86400000) {
            sessions.remove(sessionId)
            return AuthStatus.REJECTED
        }
        
        return session.status
    }

    fun approveSession(sessionId: String) {
        sessions[sessionId]?.let {
            it.status = AuthStatus.APPROVED
            updatePendingList()
        }
    }

    fun rejectSession(sessionId: String) {
        // Remove the session entirely so the client can try again (will get 401/None and restart flow)
        sessions.remove(sessionId)
        updatePendingList()
    }
    
    fun removeSession(sessionId: String) {
        sessions.remove(sessionId)
        updatePendingList()
    }

    private fun updatePendingList() {
        _pendingSessions.value = sessions.values
            .filter { it.status == AuthStatus.PENDING }
            .sortedByDescending { it.timestamp }
    }
    
    // Allow local loopback (the Android app itself) to always be approved
    fun isLocalRequest(ip: String): Boolean {
        return ip == "127.0.0.1" || ip == "0:0:0:0:0:0:0:1" || ip == "localhost"
    }
}
