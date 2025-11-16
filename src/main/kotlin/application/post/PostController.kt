package com.ranjan.application.post

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.post.model.*
import com.ranjan.domain.post.usecase.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
        val params = call.request.queryParameters

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getPostsUseCase.execute(pagination)

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
        val userId = call.principal<JWTPrincipal>()
            ?.payload?.getClaim("userId")?.asString()

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized"))
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
        val postId = call.parameters["id"]
        val userId = call.principal<JWTPrincipal>()
            ?.payload?.getClaim("userId")?.asString()

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized"))
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
        val postId = call.parameters["id"]
        val userId = call.principal<JWTPrincipal>()
            ?.payload?.getClaim("userId")?.asString()

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized"))
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
        val postId = call.parameters["id"]
        val userId = call.principal<JWTPrincipal>()
            ?.payload?.getClaim("userId")?.asString()

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized"))
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
        val postId = call.parameters["id"]
        val userId = call.principal<JWTPrincipal>()
            ?.payload?.getClaim("userId")?.asString()

        if (postId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Post id required"))
            return
        }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized"))
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
