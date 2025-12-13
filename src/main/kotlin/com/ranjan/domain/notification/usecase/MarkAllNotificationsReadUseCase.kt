package com.ranjan.domain.notification.usecase

import com.ranjan.domain.notification.repository.NotificationRepository
import java.util.UUID

class MarkAllNotificationsReadUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend fun execute(userId: UUID): Result<Long> = runCatching {
        notificationRepository.markAllAsRead(userId)
    }
}

