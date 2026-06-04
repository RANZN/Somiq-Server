package com.ranjan.chat.websocket

import com.ranjan.chat.domain.model.Message
import kotlinx.serialization.Serializable

@Serializable
sealed class SocketEvent {
    @Serializable
    data class NewMessage(val message: Message) : SocketEvent()
    
    @Serializable
    data class Typing(val userId: String, val conversationId: String) : SocketEvent()

    @Serializable
    data class ReadReceipt(val userId: String, val conversationId: String, val messageId: String) : SocketEvent()
}
