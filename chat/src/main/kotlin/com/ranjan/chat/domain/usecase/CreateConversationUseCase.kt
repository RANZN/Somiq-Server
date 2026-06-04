package com.ranjan.chat.domain.usecase

import com.ranjan.chat.domain.model.Conversation
import com.ranjan.chat.domain.model.ConversationType
import com.ranjan.chat.domain.repository.ChatRepository
import java.util.UUID

class CreateConversationUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(type: ConversationType, title: String?, participantIds: List<String>): Result<Conversation> {
        val conversationId = UUID.randomUUID().toString()
        return chatRepository.createConversation(conversationId, type, title, participantIds)
    }
}
