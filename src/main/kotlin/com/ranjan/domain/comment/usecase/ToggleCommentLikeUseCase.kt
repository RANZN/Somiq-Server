package com.ranjan.domain.comment.usecase

import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.repository.CommentRepository
import java.util.UUID

class ToggleCommentLikeUseCase(
    private val commentRepository: CommentRepository
) {
    suspend fun execute(
        userId: UUID,
        commentId: String
    ): Result<CommentResponse> = runCatching {
        commentRepository.toggleLike(userId, commentId)
    }
}

