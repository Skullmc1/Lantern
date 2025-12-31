package com.lantern.app.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

class FileServer {
    private var server: ApplicationEngine? = null

    fun start(port: Int, rootDir: File) {
        if (server != null) return

        server = embeddedServer(Netty, port = port) {
            install(CORS) {
                anyHost()
            }
            routing {
                get("/{path...}") {
                    val pathSegments = call.parameters.getAll("path") ?: emptyList()
                    val path = pathSegments.joinToString(File.separator)
                    val file = File(rootDir, path)
                    
                    if (!file.exists()) {
                         call.respondText("File not found", status = HttpStatusCode.NotFound)
                         return@get
                    }

                    if (file.isDirectory) {
                        val html = generateListing(file, pathSegments.joinToString("/"))
                        call.respondText(html, ContentType.Text.Html)
                    } else {
                        call.respondFile(file)
                    }
                }
                
                // Root handler
                get("/") {
                     val file = rootDir
                     if (file.isDirectory) {
                        val html = generateListing(file, "")
                        call.respondText(html, ContentType.Text.Html)
                    } else {
                        call.respondFile(file)
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }

    private fun generateListing(dir: File, relativePath: String): String {
        val files = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
        val sb = StringBuilder()
        
        // Modern Green Theme for the Web Interface
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Lantern - /${relativePath}</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Courier New', monospace; /* Fallback for DepartureMono */
                        background-color: #121212;
                        color: #00E676;
                        margin: 0;
                        padding: 20px;
                    }
                    h1 {
                        font-family: sans-serif; /* ModernSans fallback */
                        color: #00E676;
                        border-bottom: 2px solid #006C4C;
                        padding-bottom: 10px;
                    }
                    a {
                        color: #69F0AE;
                        text-decoration: none;
                        display: block;
                        padding: 12px;
                        border-radius: 4px;
                        margin-bottom: 4px;
                        background-color: #1E1E1E;
                    }
                    a:hover {
                        background-color: #006C4C;
                        color: #FFFFFF;
                    }
                    .icon {
                        margin-right: 10px;
                        font-weight: bold;
                    }
                    .footer {
                        margin-top: 40px;
                        color: #666;
                        font-size: 0.8em;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <h1>Index of /${relativePath}</h1>
        """.trimIndent())

        if (relativePath.isNotEmpty()) {
             sb.append("<a href=\"../\"><span class=\"icon\">&#11013;</span> .. (Parent Directory)</a>")
        }

        for (file in files) {
            val name = file.name
            val isDir = file.isDirectory
            val href = if (isDir) "$name/" else name
            val icon = if (isDir) "&#128193;" else "&#128196;" // Folder vs File emoji
            
            sb.append("<a href=\"$href\"><span class=\"icon\">$icon</span> $name</a>")
        }
        
        sb.append("""
                <div class="footer">Powered by Lantern</div>
            </body>
            </html>
        """.trimIndent())
        
        return sb.toString()
    }
}
