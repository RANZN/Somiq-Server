package com.ranjan.data.collection.model

import org.jetbrains.exposed.sql.Table

object CollectionItemTable : Table("collection_item_table") {
    object Columns {
        const val ITEM_ID = "item_id"
        const val COLLECTION_ID = "collection_id"
        const val ITEM_TYPE = "item_type"
        const val ITEM_REF_ID = "item_ref_id"
        const val ADDED_AT = "added_at"
    }

    val itemId = varchar(Columns.ITEM_ID, 50).uniqueIndex()
    val collectionId = varchar(Columns.COLLECTION_ID, 50).references(CollectionTable.collectionId, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val itemType = varchar(Columns.ITEM_TYPE, 20)
    val itemRefId = varchar(Columns.ITEM_REF_ID, 50)
    val addedAt = long(Columns.ADDED_AT)
    override val primaryKey = PrimaryKey(itemId)
    
    init {
        collectionId.index()
    }
}

