package com.ranjan.chat.api

import io.ktor.server.routing.*

fun Route.messageRoutes() {
    route("/conversations/{conversationId}/messages") {
        get {
            // Get messages in a conversation
        }
        
        post {
            // Send a new message
        }
    }
}
