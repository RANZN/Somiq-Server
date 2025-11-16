package com.ranjan.data.post.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object PostBookmarkTable : Table("post_bookmarks") {
    object Columns {
        const val USER_ID = "user_id"
        const val POST_ID = "post_id"
    }

    val userId = uuid(Columns.USER_ID).references(UserTable.userId)
    val postId = varchar(Columns.POST_ID, 50).references(PostTable.postId)
    override val primaryKey = PrimaryKey(userId, postId, name = "PK_PostBookmark")
}