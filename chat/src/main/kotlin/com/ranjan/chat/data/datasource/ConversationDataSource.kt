package com.ranjan.chat.data.datasource

import com.ranjan.chat.data.entity.ConversationTable
import com.ranjan.chat.data.entity.ParticipantTable
import com.ranjan.chat.domain.model.Conversation
import com.ranjan.chat.domain.model.ConversationType
import com.ranjan.core.db.dbQuery
import com.ranjan.core.util.TimeProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ConversationDataSource(
    private val database: Database,
    private val timeProvider: TimeProvider
) {
    suspend fun createConversation(
        id: String,
        type: ConversationType,
        title: String?,
        participantIds: List<String>
    ): Conversation = database.dbQuery {
        val now = timeProvider.now()
        ConversationTable.insert {
            it[this.id] = id
            it[this.type] = type
            it[this.title] = title
            it[this.createdAt] = now
        }

        participantIds.forEach { userId ->
            ParticipantTable.insert {
                it[this.conversationId] = id
                it[this.userId] = userId
                it[this.joinedAt] = now
            }
        }

        Conversation(id, type, title, now)
    }

    suspend fun getConversation(conversationId: String): Conversation? = database.dbQuery {
        ConversationTable.selectAll()
            .where { ConversationTable.id eq conversationId }
            .singleOrNull()
            ?.let {
                Conversation(
                    id = it[ConversationTable.id],
                    type = it[ConversationTable.type],
                    title = it[ConversationTable.title],
                    createdAt = it[ConversationTable.createdAt]
                )
            }
    }

    suspend fun getConversationsForUser(userId: String): List<Conversation> = database.dbQuery {
        (ConversationTable innerJoin ParticipantTable).selectAll()
            .where { ParticipantTable.userId eq userId }
            .map {
                Conversation(
                    id = it[ConversationTable.id],
                    type = it[ConversationTable.type],
                    title = it[ConversationTable.title],
                    createdAt = it[ConversationTable.createdAt]
                )
            }
    }
}
