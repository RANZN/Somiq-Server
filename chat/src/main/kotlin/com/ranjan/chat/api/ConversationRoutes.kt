package com.ranjan.chat.api

import io.ktor.server.routing.*

fun Route.conversationRoutes() {
    route("/conversations") {
        get {
            // Get user's conversations
        }
        
        get("/{id}") {
            // Get conversation details
        }
        
        post {
            // Create a new conversation
        }
    }
}
