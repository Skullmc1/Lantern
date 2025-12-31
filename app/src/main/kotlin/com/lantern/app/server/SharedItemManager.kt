package com.lantern.app.server

import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class SharedItem(
    val token: String,
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

class SharedItemManager {
    private val sharedItems = ConcurrentHashMap<String, SharedItem>()

    fun createShare(file: File): SharedItem {
        // Check if already shared? Maybe. For now, always create new token.
        val token = UUID.randomUUID().toString().substring(0, 8) // Short token
        val item = SharedItem(token, file, file.name, file.isDirectory)
        sharedItems[token] = item
        return item
    }

    fun getItem(token: String): SharedItem? {
        return sharedItems[token]
    }

    fun revokeShare(token: String) {
        sharedItems.remove(token)
    }

    fun getAllShares(): List<SharedItem> {
        return sharedItems.values.sortedByDescending { it.createdAt }
    }
}
