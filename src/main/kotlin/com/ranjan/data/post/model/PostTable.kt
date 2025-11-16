package com.ranjan.data.post.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object PostTable : Table("post_table") {
    object Columns {
        const val POST_ID = "post_id"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val MEDIA_URLS = "media_urls"
        const val AUTHOR_ID = "author_id"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    val postId = varchar(Columns.POST_ID, 50).uniqueIndex()
    val title = varchar(Columns.TITLE, 255)
    val content = text(Columns.CONTENT)
    val mediaUrls = text(Columns.MEDIA_URLS)
    val authorId = uuid(Columns.AUTHOR_ID).index().references(UserTable.userId)
    val createdAt = long(Columns.CREATED_AT).index()
    val updatedAt = long(Columns.UPDATED_AT).nullable()
    override val primaryKey = PrimaryKey(postId)

}