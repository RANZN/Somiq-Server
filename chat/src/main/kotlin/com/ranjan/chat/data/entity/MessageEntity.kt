package com.ranjan.chat.data.entity

import com.ranjan.chat.domain.model.MessageType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessageTable : Table("messages") {
    val id = varchar("id", 255)
    val conversationId = varchar("conversation_id", 255) references ConversationTable.id
    val senderId = varchar("sender_id", 255)
    val content = text("content")
    val type = enumeration("type", MessageType::class)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
