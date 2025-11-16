package com.ranjan.server.post

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.post.model.*
import com.ranjan.domain.post.usecase.*
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class PostController(
    private val createPostUseCase: CreatePostUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) {

    // ---------------------------------------------------------
    // GET /v1/posts
    // ---------------------------------------------------------
    suspend fun getPosts(call: ApplicationCall) {
        val userId = call.userIdOrNull()
        val params = call.request.queryParameters

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getPostsUseCase.execute(userId, pagination)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load posts")
            )
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
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Post not found"))

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

        val postRequest = try {
            call.receive<CreatePostRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = createPostUseCase.execute(userId, postRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
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
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is AccessDeniedException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))

                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Post not found"))

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
                is AccessDeniedException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))

                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Post not found"))

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
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to update like status")
            )
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
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to update bookmark status")
            )
        }
    }
}
