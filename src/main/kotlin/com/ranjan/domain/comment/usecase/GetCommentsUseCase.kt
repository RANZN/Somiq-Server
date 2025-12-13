package com.ranjan.domain.comment.usecase

import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.repository.CommentRepository
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import java.util.UUID

class GetCommentsUseCase(
    private val commentRepository: CommentRepository
) {
    suspend fun execute(
        postId: String?,
        reelId: String?,
        parentCommentId: String?,
        viewerId: UUID?,
        pagination: PaginationRequest
    ): Result<PaginationResult<CommentResponse>> = runCatching {
        commentRepository.getComments(postId, reelId, parentCommentId, viewerId, pagination)
    }
}

