package com.ranjan.data.collection.model

import com.ranjan.data.auth.model.UserTable
import org.jetbrains.exposed.sql.Table

object CollectionTable : Table("collection_table") {
    object Columns {
        const val COLLECTION_ID = "collection_id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val USER_ID = "user_id"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }

    val collectionId = varchar(Columns.COLLECTION_ID, 50).uniqueIndex()
    val name = varchar(Columns.NAME, 255)
    val description = text(Columns.DESCRIPTION).nullable()
    val userId = uuid(Columns.USER_ID).index().references(UserTable.userId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val createdAt = long(Columns.CREATED_AT).index()
    val updatedAt = long(Columns.UPDATED_AT).nullable()
    override val primaryKey = PrimaryKey(collectionId)
}

