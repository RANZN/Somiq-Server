package com.ranjan.data.notification.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object NotificationTable : Table("notification_table") {
    object Columns {
        const val NOTIFICATION_ID = "notification_id"
        const val USER_ID = "user_id"
        const val TYPE = "type"
        const val MESSAGE = "message"
        const val ACTOR_ID = "actor_id"
        const val TARGET_ID = "target_id"
        const val TARGET_TYPE = "target_type"
        const val CREATED_AT = "created_at"
        const val IS_READ = "is_read"
    }

    val notificationId = varchar(Columns.NOTIFICATION_ID, 50).uniqueIndex()
    val userId = uuid(Columns.USER_ID).index().references(UserTable.userId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val type = varchar(Columns.TYPE, 50)
    val message = text(Columns.MESSAGE)
    val actorId = uuid(Columns.ACTOR_ID).index().references(UserTable.userId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val targetId = varchar(Columns.TARGET_ID, 50).nullable()
    val targetType = varchar(Columns.TARGET_TYPE, 50).nullable()
    val createdAt = long(Columns.CREATED_AT).index()
    val isRead = bool(Columns.IS_READ).default(false)
    override val primaryKey = PrimaryKey(notificationId)
}

