package com.ranjan.chat.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    AUDIO
}

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val createdAt: Instant
)
