package com.ranjan.chat.websocket

import io.ktor.server.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ChatConnection(
    val session: DefaultWebSocketServerSession,
    val userId: String
) {
    private val _events = MutableSharedFlow<SocketEvent>()
    val events = _events.asSharedFlow()

    suspend fun send(event: SocketEvent) {
        _events.emit(event)
    }
}
