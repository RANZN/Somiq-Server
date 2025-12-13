package com.ranjan.data.story.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object StoryViewTable : Table("story_view_table") {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val STORY_ID = "story_id"
        const val VIEWED_AT = "viewed_at"
    }

    val id = long(Columns.ID).autoIncrement()
    val userId = uuid(Columns.USER_ID).references(UserTable.userId)
    val storyId = varchar(Columns.STORY_ID, 50).references(StoryTable.storyId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val viewedAt = long(Columns.VIEWED_AT)
    override val primaryKey = PrimaryKey(id)
    
    init {
        storyId.index()
    }
}

