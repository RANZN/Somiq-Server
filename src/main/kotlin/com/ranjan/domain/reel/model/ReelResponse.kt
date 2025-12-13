package com.ranjan.domain.reel.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ReelResponse(
    val reelId: String,
    val title: String,
    val description: String?,
    val videoUrl: String,
    val thumbnailUrl: String?,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val authorName: String,
    val authorUsername: String?,
    val authorProfilePictureUrl: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val likesCount: Long,
    val commentsCount: Long,
    val viewsCount: Long,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

