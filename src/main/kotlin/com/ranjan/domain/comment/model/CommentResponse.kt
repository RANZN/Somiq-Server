package com.ranjan.domain.comment.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CommentResponse(
    val commentId: String,
    val content: String,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val authorName: String,
    val authorUsername: String?,
    val authorProfilePictureUrl: String?,
    @Serializable(with = UUIDSerializer::class)
    val parentCommentId: String? = null, // For nested replies
    val createdAt: Long,
    val updatedAt: Long?,
    val likesCount: Long,
    val repliesCount: Long,
    val isLiked: Boolean = false
)

