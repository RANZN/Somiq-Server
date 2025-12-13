package com.ranjan.data.reel.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.UUID

object ReelViewTable : Table("reel_view_table") {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val REEL_ID = "reel_id"
        const val VIEWED_AT = "viewed_at"
    }

    val id = long(Columns.ID).autoIncrement()
    // userId is nullable to support anonymous views
    // Foreign key constraint removed because Exposed doesn't support nullable FK references
    val userId = uuid(Columns.USER_ID).nullable()
    val reelId = varchar(Columns.REEL_ID, 50).references(ReelTable.reelId, onDelete = ReferenceOption.CASCADE)
    val viewedAt = long(Columns.VIEWED_AT)
    override val primaryKey = PrimaryKey(id)
    
    init {
        // Index for faster queries on reelId
        // Note: userId is nullable so we index reelId separately
        // Duplicate prevention is handled in application logic
        reelId.index()
    }
}