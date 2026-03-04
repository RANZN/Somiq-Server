package com.ranjan.server.media

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.server.common.extension.userId
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.respond
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.io.File
import java.util.UUID
import kotlinx.serialization.Serializable

object MediaController {

    private const val UPLOADS_DIR = "uploads"
    private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10 MB

    init {
        File(UPLOADS_DIR).mkdirs()
    }

    suspend fun uploadMedia(call: ApplicationCall) {
        try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required to upload"))
            return
        }

        val multipart = call.receiveMultipart()
        var savedFile: File? = null
        var errorMessage: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    val name = "${UUID.randomUUID()}.$ext"
                    val file = File(UPLOADS_DIR, name)
                    try {
                        val channel = part.provider()
                        val packet = channel.readRemaining()
                        file.writeBytes(packet.readByteArray())
                        part.dispose()
                        if (file.length() > MAX_FILE_SIZE) {
                            file.delete()
                            errorMessage = "File too large"
                        } else {
                            savedFile = file
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Upload failed"
                    }
                }
                else -> part.dispose()
            }
        }

        when {
            errorMessage != null -> {
                savedFile?.delete()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(errorMessage)
                )
            }
            savedFile == null -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("No file in request. Use multipart form field 'file'.")
                )
            }
            else -> {
                val host = call.request.host()
                val port = call.request.port()
                val baseUrl = "https://$host:$port"
                val url = "$baseUrl/$UPLOADS_DIR/${savedFile.name}"
                call.respond(HttpStatusCode.Created, UploadResponse(url))
            }
        }
    }
}

@Serializable
private data class UploadResponse(val url: String)
