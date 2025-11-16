package com.ranjan.data.db

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokens : Table("refresh_tokens") {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val TOKEN = "token"
        const val EXPIRES_AT = "expires_at"
        const val CREATED_AT = "created_at"
    }

    val id = long(Columns.ID).autoIncrement()
    val userId = varchar(Columns.USER_ID, 255)
    val token = varchar(Columns.TOKEN, 512)
    val expiresAt = timestamp(Columns.EXPIRES_AT)
    val createdAt = timestamp(Columns.CREATED_AT).default(Clock.System.now())
    override val primaryKey = PrimaryKey(id)
}