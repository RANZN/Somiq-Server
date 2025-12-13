package com.ranjan.data.comment.model

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.post.model.PostTable
import com.ranjan.data.reel.model.ReelTable
import org.jetbrains.exposed.sql.Table

object CommentTable : Table("comment_table") {
    object Columns {
        const val COMMENT_ID = "comment_id"
        const val CONTENT = "content"
        const val AUTHOR_ID = "author_id"
        const val POST_ID = "post_id"
        const val REEL_ID = "reel_id"
        const val PARENT_COMMENT_ID = "parent_comment_id"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    val commentId = varchar(Columns.COMMENT_ID, 50).uniqueIndex()
    val content = text(Columns.CONTENT)
    val authorId = uuid(Columns.AUTHOR_ID).index().references(UserTable.userId)
    // postId and reelId are nullable - foreign key constraints removed (Exposed limitation with nullable FKs)
    val postId = varchar(Columns.POST_ID, 50).nullable()
    val reelId = varchar(Columns.REEL_ID, 50).nullable()
    val parentCommentId = varchar(Columns.PARENT_COMMENT_ID, 50).nullable()
    val createdAt = long(Columns.CREATED_AT).index()
    val updatedAt = long(Columns.UPDATED_AT).nullable()
    override val primaryKey = PrimaryKey(commentId)
}

