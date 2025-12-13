package com.ranjan.server.notification

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.notification.usecase.*
import com.ranjan.server.common.extension.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class NotificationController(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase,
) {

    suspend fun getNotifications(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val params = call.request.queryParameters
        val unreadOnly = params["unreadOnly"]?.toBoolean() ?: false

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getNotificationsUseCase.execute(userId, unreadOnly, pagination)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load notifications")
            )
        }
    }

    suspend fun markAsRead(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val notificationId = call.parameters["notificationId"]
        if (notificationId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Notification id required"))
            return
        }

        val result = markNotificationReadUseCase.execute(notificationId, userId)

        result.onSuccess { success ->
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Notification marked as read"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Notification not found"))
            }
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to mark notification as read")
            )
        }
    }

    suspend fun markAllAsRead(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val result = markAllNotificationsReadUseCase.execute(userId)

        result.onSuccess { count ->
            call.respond(HttpStatusCode.OK, mapOf("message" to "All notifications marked as read", "count" to count))
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to mark all notifications as read")
            )
        }
    }

    suspend fun getUnreadCount(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val result = getUnreadCountUseCase.execute(userId)

        result.onSuccess { count ->
            call.respond(HttpStatusCode.OK, mapOf("unreadCount" to count))
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to get unread count")
            )
        }
    }
}

