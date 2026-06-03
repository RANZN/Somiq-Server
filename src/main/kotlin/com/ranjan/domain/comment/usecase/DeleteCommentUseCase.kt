package com.ranjan.domain.comment.usecase

import com.ranjan.domain.comment.repository.CommentRepository
import com.ranjan.core.exception.ForbiddenException
import com.ranjan.core.exception.ResourceNotFoundException
import java.util.UUID

class DeleteCommentUseCase(
    private val commentRepository: CommentRepository
) {
    suspend fun execute(
        userId: UUID,
        commentId: String
    ): Result<Unit> = runCatching {
        val existing = commentRepository.getCommentById(commentId, userId)
            ?: throw ResourceNotFoundException("Comment not found")

        if (existing.authorId != userId) {
            throw ForbiddenException("You can only delete your own comments")
        }

        commentRepository.deleteComment(commentId)
    }
}

