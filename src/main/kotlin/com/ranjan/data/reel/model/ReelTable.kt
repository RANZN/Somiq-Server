package com.ranjan.data.reel.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object ReelTable : Table("reel_table") {
    object Columns {
        const val REEL_ID = "reel_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val VIDEO_URL = "video_url"
        const val THUMBNAIL_URL = "thumbnail_url"
        const val AUTHOR_ID = "author_id"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    val reelId = varchar(Columns.REEL_ID, 50).uniqueIndex()
    val title = varchar(Columns.TITLE, 255)
    val description = text(Columns.DESCRIPTION).nullable()
    val videoUrl = text(Columns.VIDEO_URL)
    val thumbnailUrl = text(Columns.THUMBNAIL_URL).nullable()
    val authorId = uuid(Columns.AUTHOR_ID).index().references(UserTable.userId)
    val createdAt = long(Columns.CREATED_AT).index()
    val updatedAt = long(Columns.UPDATED_AT).nullable()
    override val primaryKey = PrimaryKey(reelId)
}

