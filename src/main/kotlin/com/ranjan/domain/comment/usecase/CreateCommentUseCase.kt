package com.ranjan.domain.comment.usecase

import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.model.CreateCommentRequest
import com.ranjan.domain.comment.repository.CommentRepository
import java.util.UUID

class CreateCommentUseCase(
    private val commentRepository: CommentRepository
) {
    suspend fun execute(
        userId: UUID,
        postId: String?,
        reelId: String?,
        request: CreateCommentRequest
    ): Result<CommentResponse> = runCatching {
        if (request.content.isBlank()) {
            throw IllegalArgumentException("Comment content cannot be empty")
        }
        if (postId == null && reelId == null) {
            throw IllegalArgumentException("Either postId or reelId must be provided")
        }
        commentRepository.createComment(userId, postId, reelId, request)
    }
}

