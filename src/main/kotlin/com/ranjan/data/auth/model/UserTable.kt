package com.ranjan.data.auth.model

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object UserTable : Table("user_table") {
    object Columns {
        const val USER_ID = "user_id"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val NAME = "name"
        const val USERNAME = "username"
        const val PROFILE_PICTURE_URL = "profile_picture_url"
        const val BIO = "bio"
    }

    val userId = uuid(Columns.USER_ID).clientDefault { UUID.randomUUID() }
    val email = varchar(Columns.EMAIL, 255).nullable().uniqueIndex()
    val phone = varchar(Columns.PHONE, 32).nullable().uniqueIndex()
    val name = varchar(Columns.NAME, 255)
    val username = varchar(Columns.USERNAME, 50).uniqueIndex().nullable()
    val profilePictureUrl = text(Columns.PROFILE_PICTURE_URL).nullable()
    val bio = text(Columns.BIO).nullable()
    override val primaryKey = PrimaryKey(userId)
}