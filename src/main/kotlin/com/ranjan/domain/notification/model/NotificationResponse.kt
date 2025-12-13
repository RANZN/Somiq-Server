package com.ranjan.domain.notification.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NotificationResponse(
    val notificationId: String,
    val type: NotificationType,
    val message: String,
    @Serializable(with = UUIDSerializer::class)
    val actorId: UUID,
    val actorName: String,
    val actorUsername: String?,
    val actorProfilePictureUrl: String?,
    val targetId: String?, // postId, reelId, commentId, etc.
    val targetType: String?, // "post", "reel", "comment", etc.
    val createdAt: Long,
    val isRead: Boolean
)

@Serializable
enum class NotificationType {
    LIKE_POST,
    LIKE_REEL,
    LIKE_COMMENT,
    COMMENT_POST,
    COMMENT_REEL,
    REPLY_COMMENT,
    FOLLOW,
    MENTION
}

