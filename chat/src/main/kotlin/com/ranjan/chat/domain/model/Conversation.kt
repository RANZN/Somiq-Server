package com.ranjan.chat.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class ConversationType {
    DIRECT,
    GROUP
}

@Serializable
data class Conversation(
    val id: String,
    val type: ConversationType,
    val title: String?,
    val createdAt: Instant
)
