package com.ranjan.chat.data.repository

import com.ranjan.chat.data.datasource.ConversationDataSource
import com.ranjan.chat.data.datasource.MessageDataSource
import com.ranjan.chat.domain.model.Conversation
import com.ranjan.chat.domain.model.ConversationType
import com.ranjan.chat.domain.model.Message
import com.ranjan.chat.domain.model.MessageType
import com.ranjan.chat.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val conversationDataSource: ConversationDataSource,
    private val messageDataSource: MessageDataSource,
) : ChatRepository {

    override suspend fun createConversation(
        id: String,
        type: ConversationType,
        title: String?,
        participantIds: List<String>
    ): Result<Conversation> {
        return try {
            val conversation = conversationDataSource.createConversation(id, type, title, participantIds)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversation(conversationId: String): Result<Conversation?> {
        return try {
            val conversation = conversationDataSource.getConversation(conversationId)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversationsForUser(userId: String): Result<List<Conversation>> {
        return try {
            val conversations = conversationDataSource.getConversationsForUser(userId)
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMessage(
        id: String,
        conversationId: String,
        senderId: String,
        content: String,
        type: MessageType
    ): Result<Message> {
        return try {
            val message = messageDataSource.saveMessage(id, conversationId, senderId, content, type)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(
        conversationId: String,
        limit: Int,
        beforeCursor: String?
    ): Result<List<Message>> {
        return try {
            val messages = messageDataSource.getMessages(conversationId, limit, beforeCursor)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(
        conversationId: String,
        userId: String,
        messageId: String
    ): Result<Unit> {
        // Implement read receipt logic later
        return Result.success(Unit)
    }
}
