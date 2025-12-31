package com.lantern.app.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

@Serializable
data class FileItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long
)

@Serializable
data class DirectoryListing(
    val path: String,
    val files: List<FileItem>
)

@Serializable
data class RenameRequest(
    val path: String,
    val newName: String
)

@Serializable
data class AuthRequest(
    val deviceName: String
)

@Serializable
data class AuthResponse(
    val sessionId: String,
    val status: String
)

@Serializable
data class AuthStatusResponse(
    val status: String
)

class FileServer(
    val sessionManager: SessionManager = SessionManager(),
    val sharedItemManager: SharedItemManager = SharedItemManager()
) {
    private var server: ApplicationEngine? = null

    fun start(port: Int, rootDir: File) {
        if (server != null) return

        server = embeddedServer(Netty, port = port) {
            install(CORS) {
                anyHost()
                allowHeader(io.ktor.http.HttpHeaders.ContentType)
                allowHeader("X-Session-ID") // Allow custom auth header
                allowMethod(io.ktor.http.HttpMethod.Delete)
                allowMethod(io.ktor.http.HttpMethod.Put)
                allowMethod(io.ktor.http.HttpMethod.Post)
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                // Serve static files from the 'files' package in resources
                static("/") {
                    resources("files")
                }
                
                // Fallback/Default to index.html for root
                get("/") {
                    call.respondText(
                        this::class.java.classLoader.getResource("files/index.html")!!.readText(),
                        ContentType.Text.Html
                    )
                }

                // Auth Endpoints
                post("/api/auth/request") {
                    val request = call.receive<AuthRequest>()
                    val ip = call.request.local.remoteHost
                    val sessionId = sessionManager.requestSession(request.deviceName, ip)
                    call.respond(AuthResponse(sessionId, "PENDING"))
                }

                get("/api/auth/status") {
                    val sessionId = call.request.headers["X-Session-ID"]
                    if (sessionId.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.Unauthorized, "Missing Session ID")
                        return@get
                    }
                    val status = sessionManager.getSessionStatus(sessionId)
                    call.respond(AuthStatusResponse(status.name))
                }

                // Helper to check auth
                suspend fun checkAuth(call: io.ktor.server.application.ApplicationCall): Boolean {
                    // Check Header OR Query Param (for downloads)
                    val sessionId = call.request.headers["X-Session-ID"] 
                        ?: call.request.queryParameters["sessionId"]
                    
                    val ip = call.request.local.remoteHost
                    
                    if (sessionManager.isLocalRequest(ip)) return true
                    
                    if (sessionId.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.Unauthorized, "Missing Session ID")
                        return false
                    }
                    val status = sessionManager.getSessionStatus(sessionId)
                    if (status != AuthStatus.APPROVED) {
                        call.respond(HttpStatusCode.Forbidden, "Session not approved")
                        return false
                    }
                    return true
                }

                // --- Magic Links (Bypass Auth) ---
                get("/s/{token}") {
                    val token = call.parameters["token"]
                    val item = sharedItemManager.getItem(token ?: "")
                    
                    if (item == null) {
                        call.respond(HttpStatusCode.NotFound, "Shared link invalid or expired")
                        return@get
                    }

                    if (item.isDirectory) {
                        // Folder sharing is disabled for magic links for now
                        call.respond(HttpStatusCode.Forbidden, "Folder sharing is currently disabled")
                    } else {
                        // Direct file download
                        call.response.headers.append(
                            io.ktor.http.HttpHeaders.ContentDisposition,
                            io.ktor.http.ContentDisposition.Attachment.withParameter(
                                io.ktor.http.ContentDisposition.Parameters.FileName,
                                item.file.name
                            ).toString()
                        )
                        call.respondFile(item.file)
                    }
                }
                
                // API to list content of a shared folder (Authenticated by Token)
                get("/api/share/list") {
                    val token = call.request.queryParameters["token"]
                    val subPath = call.request.queryParameters["path"] ?: "" // Subpath inside the shared folder
                    
                    val item = sharedItemManager.getItem(token ?: "")
                    if (item == null || !item.isDirectory) {
                        call.respond(HttpStatusCode.NotFound, "Invalid share")
                        return@get
                    }
                    
                    val root = item.file
                    val target = if(subPath.isEmpty()) root else File(root, subPath.replace("..", "")) // Basic security
                    
                    // Ensure target is still inside root
                    if (!target.canonicalPath.startsWith(root.canonicalPath)) {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                        return@get
                    }

                    if (!target.exists()) {
                         call.respond(HttpStatusCode.NotFound, "Not found")
                         return@get
                    }
                    
                    if (target.isFile) {
                         // Download sub-file
                         call.response.headers.append(
                            io.ktor.http.HttpHeaders.ContentDisposition,
                            io.ktor.http.ContentDisposition.Attachment.withParameter(
                                io.ktor.http.ContentDisposition.Parameters.FileName,
                                target.name
                            ).toString()
                        )
                        call.respondFile(target)
                        return@get
                    }

                    val fileList = withContext(Dispatchers.IO) {
                        target.listFiles()?.map { 
                            FileItem(it.name, it.isDirectory, it.length()) 
                        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                    }
                    
                    call.respond(DirectoryListing(subPath, fileList))
                }


                // API for listing files (Authenticated)
                get("/api/list") {
                    if (!checkAuth(call)) return@get

                    val pathParam = call.request.queryParameters["path"] ?: ""
                    val safePath = pathParam.replace("..", "")
                    
                    val currentDir = if (safePath.isEmpty()) rootDir else File(rootDir, safePath)
                    
                    if (!currentDir.exists() || !currentDir.isDirectory) {
                        call.respond(HttpStatusCode.NotFound, "Directory not found")
                        return@get
                    }
                    
                    val fileList = withContext(Dispatchers.IO) {
                        currentDir.listFiles()?.map { 
                            FileItem(it.name, it.isDirectory, it.length()) 
                        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                    }
                    
                    call.respond(DirectoryListing(safePath, fileList))
                }
                
                // API for downloading files
                get("/api/download") {
                    if (!checkAuth(call)) return@get

                    val pathParam = call.request.queryParameters["path"] ?: ""
                    val safePath = pathParam.replace("..", "")
                    val file = File(rootDir, safePath)
                    
                    if (file.exists() && file.isFile) {
                        call.response.headers.append(
                            io.ktor.http.HttpHeaders.ContentDisposition,
                            io.ktor.http.ContentDisposition.Attachment.withParameter(
                                io.ktor.http.ContentDisposition.Parameters.FileName,
                                file.name
                            ).toString()
                        )
                        call.respondFile(file)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "File not found")
                    }
                }

                // API for uploading files
                post("/api/upload") {
                    if (!checkAuth(call)) return@post

                    val pathParam = call.request.queryParameters["path"] ?: ""
                    val safePath = pathParam.replace("..", "")
                    val targetDir = if (safePath.isEmpty()) rootDir else File(rootDir, safePath)

                    if (!targetDir.exists()) {
                        call.respond(HttpStatusCode.NotFound, "Target directory not found")
                        return@post
                    }

                    val multipart = call.receiveMultipart()
                    withContext(Dispatchers.IO) {
                        multipart.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                val fileName = part.originalFileName as String
                                val file = File(targetDir, fileName)
                                part.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                            part.dispose()
                        }
                    }
                    call.respond(HttpStatusCode.OK, "Uploaded")
                }

                // API for deleting files
                delete("/api/delete") {
                    if (!checkAuth(call)) return@delete

                    val pathParam = call.request.queryParameters["path"] ?: ""
                    if (pathParam.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, "Path required")
                        return@delete
                    }
                    val safePath = pathParam.replace("..", "")
                    val file = File(rootDir, safePath)
                    
                    if (file.exists()) {
                        val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "Deleted")
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Failed to delete")
                        }
                    } else {
                        call.respond(HttpStatusCode.NotFound, "File not found")
                    }
                }

                // API for renaming files
                post("/api/rename") {
                    if (!checkAuth(call)) return@post

                    val request = call.receive<RenameRequest>()
                    val safePath = request.path.replace("..", "")
                    val safeNewName = request.newName.replace("/", "").replace("\\", "") // simple sanitization
                    
                    val file = File(rootDir, safePath)
                    if (!file.exists()) {
                        call.respond(HttpStatusCode.NotFound, "File not found")
                        return@post
                    }

                    val newFile = File(file.parentFile, safeNewName)
                    if (newFile.exists()) {
                        call.respond(HttpStatusCode.Conflict, "File with that name already exists")
                        return@post
                    }

                    if (file.renameTo(newFile)) {
                         call.respond(HttpStatusCode.OK, "Renamed")
                    } else {
                         call.respond(HttpStatusCode.InternalServerError, "Failed to rename")
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
}
