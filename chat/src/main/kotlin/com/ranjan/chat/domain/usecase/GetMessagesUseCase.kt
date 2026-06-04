package com.ranjan.chat.domain.usecase

import com.ranjan.chat.domain.model.Message
import com.ranjan.chat.domain.repository.ChatRepository

class GetMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(conversationId: String, limit: Int, beforeCursor: String?): Result<List<Message>> {
        return chatRepository.getMessages(conversationId, limit, beforeCursor)
    }
}
