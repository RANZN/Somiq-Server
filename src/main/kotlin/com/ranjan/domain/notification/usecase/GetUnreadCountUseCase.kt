package com.ranjan.domain.notification.usecase

import com.ranjan.domain.notification.repository.NotificationRepository
import java.util.UUID

class GetUnreadCountUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend fun execute(userId: UUID): Result<Long> = runCatching {
        notificationRepository.getUnreadCount(userId)
    }
}

