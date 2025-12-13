package com.ranjan.domain.comment.repository

import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.model.CreateCommentRequest
import com.ranjan.domain.comment.model.UpdateCommentRequest
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import java.util.UUID

interface CommentRepository {
    suspend fun createComment(
        userId: UUID,
        postId: String?,
        reelId: String?,
        request: CreateCommentRequest
    ): CommentResponse

    suspend fun getComments(
        postId: String?,
        reelId: String?,
        parentCommentId: String?,
        viewerId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<CommentResponse>

    suspend fun getCommentById(commentId: String, viewerId: UUID?): CommentResponse?

    suspend fun updateComment(commentId: String, request: UpdateCommentRequest): CommentResponse

    suspend fun deleteComment(commentId: String)

    suspend fun exists(commentId: String): Boolean

    suspend fun toggleLike(userId: UUID, commentId: String): CommentResponse

    suspend fun getRepliesCount(commentId: String): Long
}

