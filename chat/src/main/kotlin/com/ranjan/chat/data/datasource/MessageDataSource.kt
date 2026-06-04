package com.ranjan.chat.data.datasource

import com.ranjan.chat.data.entity.MessageTable
import com.ranjan.chat.domain.model.Message
import com.ranjan.chat.domain.model.MessageType
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class MessageDataSource(private val database: Database) {
    suspend fun saveMessage(id: String, conversationId: String, senderId: String, content: String, type: MessageType): Message {
        return transaction(database) {
            val now = Clock.System.now()
            MessageTable.insert {
                it[this.id] = id
                it[this.conversationId] = conversationId
                it[this.senderId] = senderId
                it[this.content] = content
                it[this.type] = type
                it[this.createdAt] = now
            }
            Message(id, conversationId, senderId, content, type, now)
        }
    }

    suspend fun getMessages(conversationId: String, limit: Int, beforeCursor: String?): List<Message> {
        return transaction(database) {
            val query = MessageTable.selectAll().where { MessageTable.conversationId eq conversationId }
            
            if (beforeCursor != null) {
                val cursorTimestamp = MessageTable.selectAll().where { MessageTable.id eq beforeCursor }.single()[MessageTable.createdAt]
                query.andWhere { MessageTable.createdAt less cursorTimestamp }
            }
            
            query.orderBy(MessageTable.createdAt, SortOrder.DESC)
                .limit(limit)
                .map {
                    Message(
                        id = it[MessageTable.id],
                        conversationId = it[MessageTable.conversationId],
                        senderId = it[MessageTable.senderId],
                        content = it[MessageTable.content],
                        type = it[MessageTable.type],
                        createdAt = it[MessageTable.createdAt]
                    )
                }
        }
    }
}
