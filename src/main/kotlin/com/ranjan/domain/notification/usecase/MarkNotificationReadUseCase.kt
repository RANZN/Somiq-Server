package com.ranjan.domain.notification.usecase

import com.ranjan.domain.notification.repository.NotificationRepository
import java.util.UUID

class MarkNotificationReadUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend fun execute(notificationId: String, userId: UUID): Result<Boolean> = runCatching {
        notificationRepository.markAsRead(notificationId, userId)
    }
}

