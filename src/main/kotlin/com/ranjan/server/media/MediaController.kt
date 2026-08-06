package com.ranjan.server.media

import com.ranjan.core.model.ErrorResponse
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.baseUrl
import com.ranjan.server.common.extension.getExtension
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

class MediaController(
    private val mediaStorageService: MediaStorageService,
    private val userRepository: UserRepository
) {



    suspend fun uploadMedia(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required to upload"))
            return
        }

        val user = userRepository.findById(userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            return
        }

        val type = call.request.queryParameters["type"]
        val multipart = call.receiveMultipart()
        var fileName: String? = null
        var errorMessage: String? = null
        val postTimePrefix = Clock.System.now().toEpochMilliseconds().toString()
        val subDir = if (type == "profile_pic") {
            "${user.username}/profile_pic"
        } else {
            "${user.username}/posts/$postTimePrefix"
        }

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    try {
                        val ext = part.getExtension()
                        part.provider().toInputStream().use { stream ->
                            fileName = mediaStorageService.saveStream(stream, ext, subDir)
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
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(errorMessage)
                )
            }
            fileName == null -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("No file in request. Use multipart form field 'file'.")
                )
            }
            else -> {
                val url = mediaStorageService.getUrlForFile(call.baseUrl(), subDir, fileName)
                call.respond(HttpStatusCode.Created, UploadResponse(url))
            }
        }
    }
}

@Serializable
private data class UploadResponse(val url: String)
