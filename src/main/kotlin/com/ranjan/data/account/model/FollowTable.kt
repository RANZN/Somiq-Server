package com.ranjan.data.account.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object FollowTable : Table("follow_table") {
    object Columns {
        const val FOLLOWER_ID = "follower_id"
        const val FOLLOWING_ID = "following_id"
    }

    val followerId = uuid(Columns.FOLLOWER_ID).references(UserTable.userId)
    val followingId = uuid(Columns.FOLLOWING_ID).references(UserTable.userId)
    override val primaryKey = PrimaryKey(followerId, followingId)
}

