package com.ranjan.chat.data.entity

import com.ranjan.chat.domain.model.ConversationType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ConversationTable : Table("conversations") {
    val id = varchar("id", 255)
    val type = enumeration("type", ConversationType::class)
    val title = varchar("title", 255).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
