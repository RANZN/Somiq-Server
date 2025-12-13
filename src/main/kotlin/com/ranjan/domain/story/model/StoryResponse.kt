package com.ranjan.domain.story.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StoryResponse(
    val storyId: String,
    val mediaUrl: String,
    val mediaType: MediaType,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val authorName: String,
    val authorUsername: String?,
    val authorProfilePictureUrl: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val viewsCount: Long,
    val isViewed: Boolean = false
)

@Serializable
enum class MediaType {
    IMAGE,
    VIDEO
}

