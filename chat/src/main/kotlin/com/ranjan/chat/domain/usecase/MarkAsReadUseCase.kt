package com.ranjan.chat.domain.usecase

import com.ranjan.chat.domain.repository.ChatRepository

class MarkAsReadUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(conversationId: String, userId: String, messageId: String): Result<Unit> {
        return chatRepository.markAsRead(conversationId, userId, messageId)
    }
}
