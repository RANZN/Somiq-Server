package com.ranjan.chat.websocket

import java.util.concurrent.ConcurrentHashMap

class ChatConnectionManager {
    private val connections = ConcurrentHashMap<String, MutableSet<ChatConnection>>()

    fun addConnection(userId: String, connection: ChatConnection) {
        val userConnections = connections.getOrPut(userId) { mutableSetOf() }
        userConnections.add(connection)
    }

    fun removeConnection(userId: String, connection: ChatConnection) {
        connections[userId]?.remove(connection)
        if (connections[userId]?.isEmpty() == true) {
            connections.remove(userId)
        }
    }

    fun getConnections(userId: String): Set<ChatConnection> {
        return connections[userId]?.toSet() ?: emptySet()
    }
}