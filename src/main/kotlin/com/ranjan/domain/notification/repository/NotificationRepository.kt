package com.ranjan.domain.notification.repository

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.notification.model.NotificationResponse
import com.ranjan.domain.notification.model.NotificationType
import java.util.UUID

interface NotificationRepository {
    suspend fun createNotification(
        userId: UUID,
        type: NotificationType,
        message: String,
        actorId: UUID,
        targetId: String? = null,
        targetType: String? = null
    ): NotificationResponse

    suspend fun getNotifications(
        userId: UUID,
        unreadOnly: Boolean = false,
        pagination: PaginationRequest
    ): PaginationResult<NotificationResponse>

    suspend fun markAsRead(notificationId: String, userId: UUID): Boolean
    suspend fun markAllAsRead(userId: UUID): Long
    suspend fun getUnreadCount(userId: UUID): Long
}

