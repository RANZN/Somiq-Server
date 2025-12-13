package com.ranjan.domain.notification.usecase

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.notification.model.NotificationResponse
import com.ranjan.domain.notification.repository.NotificationRepository
import java.util.UUID

class GetNotificationsUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend fun execute(
        userId: UUID,
        unreadOnly: Boolean,
        pagination: PaginationRequest
    ): Result<PaginationResult<NotificationResponse>> = runCatching {
        notificationRepository.getNotifications(userId, unreadOnly, pagination)
    }
}

