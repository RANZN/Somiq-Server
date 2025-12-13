package com.ranjan.data.notification.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.notification.model.NotificationTable
import com.ranjan.data.sources.db.dbQuery
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.notification.model.NotificationResponse
import com.ranjan.domain.notification.model.NotificationType
import com.ranjan.domain.notification.repository.NotificationRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class NotificationRepositoryImpl(
    private val db: Database
) : NotificationRepository {

    override suspend fun createNotification(
        userId: UUID,
        type: NotificationType,
        message: String,
        actorId: UUID,
        targetId: String?,
        targetType: String?
    ): NotificationResponse = db.dbQuery {
        val notificationId = UUID.randomUUID().toString()

        NotificationTable.insert { row ->
            row[NotificationTable.notificationId] = notificationId
            row[NotificationTable.userId] = userId
            row[NotificationTable.type] = type.name
            row[NotificationTable.message] = message
            row[NotificationTable.actorId] = actorId
            row[NotificationTable.targetId] = targetId
            row[NotificationTable.targetType] = targetType
            row[NotificationTable.createdAt] = System.currentTimeMillis()
            row[NotificationTable.isRead] = false
        }

        buildNotificationResponse(notificationId)
    }

    override suspend fun getNotifications(
        userId: UUID,
        unreadOnly: Boolean,
        pagination: PaginationRequest
    ): PaginationResult<NotificationResponse> = db.dbQuery {
        val query = NotificationTable.selectAll()
            .where { NotificationTable.userId eq userId }

        if (unreadOnly) {
            query.andWhere { NotificationTable.isRead eq false }
        }

        query.orderBy(NotificationTable.createdAt, SortOrder.DESC)

        pagination.cursor?.toLongOrNull()?.let { after ->
            query.andWhere { NotificationTable.createdAt less after }
        }

        val notificationIds = query.limit(pagination.limit).map { it[NotificationTable.notificationId] }
        if (notificationIds.isEmpty()) return@dbQuery PaginationResult(emptyList(), null)

        val notificationData = NotificationTable.selectAll()
            .where { NotificationTable.notificationId inList notificationIds }
            .associateBy { it[NotificationTable.notificationId] }

        val actorIds = notificationData.values.map { it[NotificationTable.actorId] }.distinct()
        val actors = UserTable.selectAll().where { UserTable.userId inList actorIds }
            .associateBy { it[UserTable.userId] }

        val items = notificationIds.map { notificationId ->
            val row = notificationData[notificationId]!!
            val actor = actors[row[NotificationTable.actorId]]!!
            NotificationResponse(
                notificationId = notificationId,
                type = NotificationType.valueOf(row[NotificationTable.type]),
                message = row[NotificationTable.message],
                actorId = row[NotificationTable.actorId],
                actorName = actor[UserTable.name],
                actorUsername = actor[UserTable.username],
                actorProfilePictureUrl = actor[UserTable.profilePictureUrl],
                targetId = row[NotificationTable.targetId],
                targetType = row[NotificationTable.targetType],
                createdAt = row[NotificationTable.createdAt],
                isRead = row[NotificationTable.isRead]
            )
        }

        val nextCursor = items.lastOrNull()?.createdAt?.toString()
        PaginationResult(items, nextCursor)
    }

    override suspend fun markAsRead(notificationId: String, userId: UUID): Boolean = db.dbQuery {
        val updated = NotificationTable.update(
            where = { (NotificationTable.notificationId eq notificationId) and (NotificationTable.userId eq userId) }
        ) {
            it[NotificationTable.isRead] = true
        }
        updated > 0
    }

    override suspend fun markAllAsRead(userId: UUID): Long = db.dbQuery {
        NotificationTable.update(
            where = { (NotificationTable.userId eq userId) and (NotificationTable.isRead eq false) }
        ) {
            it[NotificationTable.isRead] = true
        }.toLong()
    }

    override suspend fun getUnreadCount(userId: UUID): Long = db.dbQuery {
        NotificationTable.selectAll()
            .where { (NotificationTable.userId eq userId) and (NotificationTable.isRead eq false) }
            .count()
    }

    private fun buildNotificationResponse(notificationId: String): NotificationResponse {
        val notification = (NotificationTable innerJoin UserTable)
            .selectAll()
            .where { NotificationTable.notificationId eq notificationId }
            .single()

        val actor = UserTable.selectAll()
            .where { UserTable.userId eq notification[NotificationTable.actorId] }
            .single()

        return NotificationResponse(
            notificationId = notificationId,
            type = NotificationType.valueOf(notification[NotificationTable.type]),
            message = notification[NotificationTable.message],
            actorId = notification[NotificationTable.actorId],
            actorName = actor[UserTable.name],
            actorUsername = actor[UserTable.username],
            actorProfilePictureUrl = actor[UserTable.profilePictureUrl],
            targetId = notification[NotificationTable.targetId],
            targetType = notification[NotificationTable.targetType],
            createdAt = notification[NotificationTable.createdAt],
            isRead = notification[NotificationTable.isRead]
        )
    }
}

