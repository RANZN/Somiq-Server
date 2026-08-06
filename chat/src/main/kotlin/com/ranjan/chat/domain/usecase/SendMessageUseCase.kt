package com.ranjan.chat.domain.usecase

import com.ranjan.chat.domain.model.Message
import com.ranjan.chat.domain.model.MessageType
import com.ranjan.chat.domain.repository.ChatRepository
import java.util.UUID

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(conversationId: String, senderId: String, content: String, type: MessageType): Result<Message> {
        val messageId = UUID.randomUUID().toString()
        return chatRepository.saveMessage(messageId, conversationId, senderId, content, type)
    }
}
