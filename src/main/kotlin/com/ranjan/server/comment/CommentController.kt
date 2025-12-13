package com.ranjan.server.comment

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.comment.model.CreateCommentRequest
import com.ranjan.domain.comment.model.UpdateCommentRequest
import com.ranjan.domain.comment.usecase.*
import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class CommentController(
    private val createCommentUseCase: CreateCommentUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val toggleLikeUseCase: ToggleCommentLikeUseCase,
) {

    suspend fun getComments(call: ApplicationCall) {
        val postId = call.request.queryParameters["postId"]
        val reelId = call.request.queryParameters["reelId"]
        val parentCommentId = call.request.queryParameters["parentCommentId"]
        val viewerId = call.userIdOrNull()
        val params = call.request.queryParameters

        if (postId == null && reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Either postId or reelId is required"))
            return
        }

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getCommentsUseCase.execute(postId, reelId, parentCommentId, viewerId, pagination)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load comments")
            )
        }
    }

    suspend fun createComment(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required to comment"))
            return
        }

        val postId = call.request.queryParameters["postId"]
        val reelId = call.request.queryParameters["reelId"]

        if (postId == null && reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Either postId or reelId is required"))
            return
        }

        val commentRequest = try {
            call.receive<CreateCommentRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = createCommentUseCase.execute(userId, postId, reelId, commentRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to create comment")
            )
        }
    }

    suspend fun updateComment(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val commentId = call.parameters["commentId"]
        if (commentId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Comment id required"))
            return
        }

        val updateRequest = try {
            call.receive<UpdateCommentRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = updateCommentUseCase.execute(userId, commentId, updateRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Comment not found"))
                else ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(ex.message ?: "Failed to update comment")
                    )
            }
        }
    }

    suspend fun deleteComment(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val commentId = call.parameters["commentId"]
        if (commentId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Comment id required"))
            return
        }

        val result = deleteCommentUseCase.execute(userId, commentId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Comment deleted"))
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Comment not found"))
                else ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(ex.message ?: "Failed to delete comment")
                    )
            }
        }
    }

    suspend fun toggleLike(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required to like"))
            return
        }

        val commentId = call.parameters["commentId"]
        if (commentId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Comment id required"))
            return
        }

        val result = toggleLikeUseCase.execute(userId, commentId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to update like status")
            )
        }
    }
}

