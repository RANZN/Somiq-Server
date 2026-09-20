package com.ranjan.data.auth.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokenTable : Table("refresh_tokens") {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val TOKEN = "token"
        const val DEVICE_ID = "device_id"
        const val CREATED_AT = "created_at"
    }

    val id = long(Columns.ID).autoIncrement()
    val userId = varchar(Columns.USER_ID, 255)
    val token = varchar(Columns.TOKEN, 512)
    val deviceId = varchar(Columns.DEVICE_ID, 128).nullable()
    val createdAt = timestamp(Columns.CREATED_AT)
    override val primaryKey = PrimaryKey(id)
}