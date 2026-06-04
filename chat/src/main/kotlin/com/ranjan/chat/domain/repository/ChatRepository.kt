package com.ranjan.chat.domain.repository

import com.ranjan.chat.domain.model.Conversation
import com.ranjan.chat.domain.model.ConversationType
import com.ranjan.chat.domain.model.Message
import com.ranjan.chat.domain.model.MessageType

interface ChatRepository {
    suspend fun createConversation(id: String, type: ConversationType, title: String?, participantIds: List<String>): Result<Conversation>
    suspend fun getConversation(conversationId: String): Result<Conversation?>
    suspend fun getConversationsForUser(userId: String): Result<List<Conversation>>
    
    suspend fun saveMessage(id: String, conversationId: String, senderId: String, content: String, type: MessageType): Result<Message>
    suspend fun getMessages(conversationId: String, limit: Int, beforeCursor: String?): Result<List<Message>>
    
    suspend fun markAsRead(conversationId: String, userId: String, messageId: String): Result<Unit>
}
