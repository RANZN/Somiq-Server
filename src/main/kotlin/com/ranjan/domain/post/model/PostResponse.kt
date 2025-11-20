package com.ranjan.domain.post.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PostResponse(
    val postId: String,
    val title: String,
    val content: String,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val createdAt: Long,
    val updatedAt: Long?,
    val mediaUrls: List<String>,

    val likesCount: Long,
    val bookmarksCount: Long,

    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)