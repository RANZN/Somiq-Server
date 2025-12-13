package com.ranjan.data.comment.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object CommentLikeTable : Table("comment_like_table") {
    object Columns {
        const val USER_ID = "user_id"
        const val COMMENT_ID = "comment_id"
    }

    val userId = uuid(Columns.USER_ID).references(UserTable.userId)
    val commentId = varchar(Columns.COMMENT_ID, 50).references(CommentTable.commentId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(userId, commentId)
}

