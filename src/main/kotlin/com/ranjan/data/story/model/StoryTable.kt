package com.ranjan.data.story.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object StoryTable : Table("story_table") {
    object Columns {
        const val STORY_ID = "story_id"
        const val MEDIA_URL = "media_url"
        const val MEDIA_TYPE = "media_type"
        const val AUTHOR_ID = "author_id"
        const val CREATED_AT = "created_at"
        const val EXPIRES_AT = "expires_at"
    }

    val storyId = varchar(Columns.STORY_ID, 50).uniqueIndex()
    val mediaUrl = text(Columns.MEDIA_URL)
    val mediaType = varchar(Columns.MEDIA_TYPE, 20)
    val authorId = uuid(Columns.AUTHOR_ID).index().references(UserTable.userId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val createdAt = long(Columns.CREATED_AT).index()
    val expiresAt = long(Columns.EXPIRES_AT).index()
    override val primaryKey = PrimaryKey(storyId)
}

