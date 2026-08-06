package com.ranjan.server.post

import com.ranjan.core.exception.ForbiddenException
import com.ranjan.core.exception.ResourceNotFoundException
import com.ranjan.core.exception.UnauthorizedException
import com.ranjan.core.model.ErrorResponse
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.post.model.CreatePostRequest
import com.ranjan.domain.post.model.UpdatePostRequest
import com.ranjan.domain.post.model.withAbsoluteUrls
import com.ranjan.domain.post.usecase.*
import com.ranjan.server.common.extension.getExtension
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import com.ranjan.server.media.MediaStorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import java.util.*

class PostController(
    private val createPostUseCase: CreatePostUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val getPostsByAuthorUseCase: GetPostsByAuthorUseCase,
    private val getBookmarkedPostsUseCase: GetBookmarkedPostsUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val mediaStorageService: MediaStorageService,
) {

    // ---------------------------------------------------------
    // GET /v1/posts  (optional ?authorId= for user's posts)
    // Page 1 (no "after" cursor): public. Other pages: auth required.
    // ---------------------------------------------------------
    suspend fun getPosts(call: ApplicationCall) {
        val params = call.request.queryParameters
        val isFirstPage = params["after"].isNullOrBlank()

        val viewerId = if (isFirstPage) {
            call.userIdOrNull()
        } else {
            runCatching { call.userId() }.getOrElse { throw UnauthorizedException() }
        }

        val authorIdParam = params["authorId"]

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = when {
            authorIdParam != null -> {
                try {
                    val authorId = UUID.fromString(authorIdParam)
                    getPostsByAuthorUseCase.execute(authorId, viewerId, pagination)
                } catch (_: Exception) {
                    Result.failure(IllegalArgumentException("Invalid authorId"))
                }
            }

            else -> getPostsUseCase.execute(viewerId, pagination)
        }

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it.withAbsoluteUrls(call))
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load posts")
            )
        }
    }

    // ---------------------------------------------------------
    // GET /v1/posts/bookmarks  (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun getBookmarkedPosts(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }
        val params = call.request.queryParameters
        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )
        getBookmarkedPostsUseCase.execute(userId, pagination)
            .onSuccess { call.respond(HttpStatusCode.OK, it.withAbsoluteUrls(call)) }
            .onFailure {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to load bookmarked posts"))
            }
    }

    // ---------------------------------------------------------
    // GET /v1/posts/{id}
    // ---------------------------------------------------------
    suspend fun getPost(call: ApplicationCall) {
        val postId = call.parameters["id"]

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        val result = getPostByIdUseCase.execute(postId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it.withAbsoluteUrls(call))
        }.onFailure { ex ->
            when (ex) {
                is ResourceNotFoundException -> throw ex
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to fetch post")
                    )
            }
        }
    }

    // ---------------------------------------------------------
    // POST /v1/posts  (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun createPost(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login First to create post"))
            return
        }

        val postTimePrefix = System.currentTimeMillis().toString()
        val subDir = "${userId}/posts/$postTimePrefix"

        val caption: String
        val savedUrls: List<String>

        if (call.request.contentType().match(ContentType.MultiPart.Any)) {
            val multipart = call.receiveMultipart()
            var formCaption = ""
            val urls = mutableListOf<String>()
            var errorMessage: String? = null

            try {
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "caption" -> formCaption = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            val ext = part.getExtension()
                            val fileName = part.provider().toInputStream().use { stream ->
                                mediaStorageService.saveStream(stream, ext, subDir)
                            }
                            urls.add("uploads/$subDir/$fileName")
                        }
                        else -> part.dispose()
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "File upload failed"
            }

            if (errorMessage != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(errorMessage)
                )
                return
            }
            caption = formCaption
            savedUrls = urls
        } else {
            try {
                val jsonRequest = call.receive<CreatePostRequest>()
                caption = jsonRequest.caption
                savedUrls = jsonRequest.mediaUrls.map { bytes ->
                    val fileName = mediaStorageService.saveBytes(bytes, subDir)
                    "uploads/$subDir/$fileName"
                }
            } catch (_: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request format")
                )
                return
            }
        }

        val result = createPostUseCase.execute(userId, caption, savedUrls)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it.withAbsoluteUrls(call))
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ex.message ?: "Failed to create post")
            )
        }
    }

    // ---------------------------------------------------------
    // PUT /v1/posts/{id}  (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun updatePost(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.message ?: "Unauthorized"))
            return
        }

        val postId = call.parameters["id"]
        if (postId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        val updateRequest = try {
            call.receive<UpdatePostRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = updatePostUseCase.execute(userId, postId, updateRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it.withAbsoluteUrls(call))
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException, is ResourceNotFoundException -> throw ex
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to update post")
                    )
            }
        }
    }

    // ---------------------------------------------------------
    // DELETE /v1/posts/{id}  (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun deletePost(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.message ?: "Unauthorized"))
            return
        }

        val postId = call.parameters["id"]
        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        val result = deletePostUseCase.execute(userId, postId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, { "Post deleted" })
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException, is ResourceNotFoundException -> throw ex
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to delete post")
                    )
            }
        }
    }

    // ---------------------------------------------------------
    // POST /v1/posts/{id}/like (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun toggleLike(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login to perform Like"))
            return
        }

        val postId = call.parameters["id"]

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        val result = toggleLikeUseCase.execute(userId, postId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is ResourceNotFoundException -> throw ex
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to update like status")
                    )
            }
        }
    }

    // ---------------------------------------------------------
    // POST /v1/posts/{id}/bookmark (AUTH REQUIRED)
    // ---------------------------------------------------------
    suspend fun toggleBookmark(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login to Bookmark posts"))
            return
        }

        val postId = call.parameters["id"]

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        val result = toggleBookmarkUseCase.execute(userId, postId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is ResourceNotFoundException -> throw ex
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to update bookmark status")
                    )
            }
        }
    }
}