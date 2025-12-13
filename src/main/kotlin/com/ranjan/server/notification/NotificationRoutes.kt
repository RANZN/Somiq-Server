package com.ranjan.server.notification

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.notificationRoutes() {
    val notificationController by inject<NotificationController>()
    routing {
        authenticate(JwtConfig.NAME) {
            route("/v1/notifications") {
                get { notificationController.getNotifications(call) }
                get("/unread-count") { notificationController.getUnreadCount(call) }
                put("/{notificationId}/read") { notificationController.markAsRead(call) }
                put("/read-all") { notificationController.markAllAsRead(call) }
            }
        }
    }
}

