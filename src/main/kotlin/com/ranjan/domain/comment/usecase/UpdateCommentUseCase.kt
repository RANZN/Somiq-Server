package com.ranjan.domain.comment.usecase

import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.model.UpdateCommentRequest
import com.ranjan.domain.comment.repository.CommentRepository
import com.ranjan.domain.common.exceptions.ForbiddenException
import io.ktor.server.plugins.NotFoundException
import java.util.UUID

class UpdateCommentUseCase(
    private val commentRepository: CommentRepository
) {
    suspend fun execute(
        userId: UUID,
        commentId: String,
        request: UpdateCommentRequest
    ): Result<CommentResponse> = runCatching {
        val existing = commentRepository.getCommentById(commentId, userId)
            ?: throw NotFoundException("Comment not found")

        if (existing.authorId != userId) {
            throw ForbiddenException("You can only update your own comments")
        }

        commentRepository.updateComment(commentId, request)
    }
}

