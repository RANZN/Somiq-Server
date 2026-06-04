package com.ranjan.chat.api

import io.ktor.server.routing.*

fun Route.chatSocketRoutes() {
    route("/chat/ws") {
        // WebSocket connection for real-time chat
    }
}
