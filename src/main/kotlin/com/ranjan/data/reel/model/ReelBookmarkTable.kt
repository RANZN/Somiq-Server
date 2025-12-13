package com.ranjan.data.reel.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object ReelBookmarkTable : Table("reel_bookmark_table") {
    object Columns {
        const val USER_ID = "user_id"
        const val REEL_ID = "reel_id"
    }

    val userId = uuid(Columns.USER_ID).references(UserTable.userId)
    val reelId = varchar(Columns.REEL_ID, 50).references(ReelTable.reelId)
    override val primaryKey = PrimaryKey(userId, reelId)
}

