package com.ranjan.chat.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ConversationParticipant(
    val conversationId: String,
    val userId: String,
    val joinedAt: Instant
)
