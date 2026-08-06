package com.ranjan.chat.data.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ParticipantTable : Table("participants") {
    val conversationId = varchar("conversation_id", 255) references ConversationTable.id
    val userId = varchar("user_id", 255)
    val joinedAt = timestamp("joined_at")

    override val primaryKey = PrimaryKey(conversationId, userId)
}
