package com.ranjan.data.collection.repository

import com.ranjan.data.collection.model.CollectionItemTable
import com.ranjan.data.collection.model.CollectionTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.data.util.TimeProvider
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.collection.model.*
import com.ranjan.domain.collection.repository.CollectionRepository
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class CollectionRepositoryImpl(
    private val db: Database,
    private val timeProvider: TimeProvider,
) : CollectionRepository {

    override suspend fun createCollection(userId: UUID, name: String, description: String?): CollectionResponse = db.dbQuery {
        val collectionId = UUID.randomUUID().toString()
        val now = timeProvider.nowMillis()

        CollectionTable.insert { row ->
            row[CollectionTable.collectionId] = collectionId
            row[CollectionTable.name] = name
            row[CollectionTable.description] = description
            row[CollectionTable.userId] = userId
            row[CollectionTable.createdAt] = now
            row[CollectionTable.updatedAt] = now
        }

        CollectionResponse(
            collectionId = collectionId,
            name = name,
            description = description,
            itemsCount = 0,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun getCollections(userId: UUID): List<CollectionResponse> = db.dbQuery {
        CollectionTable.selectAll()
            .where { CollectionTable.userId eq userId }
            .orderBy(CollectionTable.updatedAt, SortOrder.DESC_NULLS_LAST)
            .map { row ->
                val collectionId = row[CollectionTable.collectionId]
                CollectionResponse(
                    collectionId = collectionId,
                    name = row[CollectionTable.name],
                    description = row[CollectionTable.description],
                    itemsCount = getItemsCount(collectionId),
                    createdAt = row[CollectionTable.createdAt],
                    updatedAt = row[CollectionTable.updatedAt]
                )
            }
    }

    override suspend fun getCollectionById(collectionId: String, userId: UUID): CollectionResponse? = db.dbQuery {
        CollectionTable.selectAll()
            .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
            .singleOrNull()?.let { row ->
                CollectionResponse(
                    collectionId = collectionId,
                    name = row[CollectionTable.name],
                    description = row[CollectionTable.description],
                    itemsCount = getItemsCount(collectionId),
                    createdAt = row[CollectionTable.createdAt],
                    updatedAt = row[CollectionTable.updatedAt]
                )
            }
    }

    override suspend fun updateCollection(collectionId: String, userId: UUID, name: String?, description: String?): CollectionResponse = db.dbQuery {
        val existing = CollectionTable.selectAll()
            .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
            .singleOrNull() ?: throw NotFoundException("Collection not found")

        CollectionTable.update({ CollectionTable.collectionId eq collectionId }) { row ->
            row[CollectionTable.name] = name ?: existing[CollectionTable.name]
            row[CollectionTable.description] = description ?: existing[CollectionTable.description]
            row[CollectionTable.updatedAt] = timeProvider.nowMillis()
        }

        getCollectionById(collectionId, userId)!!
    }

    override suspend fun deleteCollection(collectionId: String, userId: UUID) {
        db.dbQuery {
            val exists = CollectionTable.selectAll()
                .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
                .any()

            if (!exists) throw NotFoundException("Collection not found")

            CollectionItemTable.deleteWhere { CollectionItemTable.collectionId eq collectionId }
            CollectionTable.deleteWhere { CollectionTable.collectionId eq collectionId }
        }
    }

    override suspend fun addItem(collectionId: String, userId: UUID, itemType: String, itemRefId: String): CollectionItemResponse = db.dbQuery {
        // Verify collection belongs to user
        val collection = CollectionTable.selectAll()
            .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
            .singleOrNull() ?: throw NotFoundException("Collection not found")

        // Check if item already exists
        val existing = CollectionItemTable.selectAll()
            .where {
                (CollectionItemTable.collectionId eq collectionId) and
                (CollectionItemTable.itemType eq itemType) and
                (CollectionItemTable.itemRefId eq itemRefId)
            }
            .singleOrNull()

        if (existing != null) {
            throw IllegalStateException("Item already in collection")
        }

        val itemId = UUID.randomUUID().toString()
        CollectionItemTable.insert { row ->
            row[CollectionItemTable.itemId] = itemId
            row[CollectionItemTable.collectionId] = collectionId
            row[CollectionItemTable.itemType] = itemType
            row[CollectionItemTable.itemRefId] = itemRefId
            row[CollectionItemTable.addedAt] = timeProvider.nowMillis()
        }

        // Update collection updatedAt
        CollectionTable.update({ CollectionTable.collectionId eq collectionId }) {
            it[CollectionTable.updatedAt] = timeProvider.nowMillis()
        }

        CollectionItemResponse(
            itemId = itemId,
            collectionId = collectionId,
            itemType = ItemType.valueOf(itemType),
            itemRefId = itemRefId,
            addedAt = timeProvider.nowMillis()
        )
    }

    override suspend fun removeItem(collectionId: String, itemId: String, userId: UUID) {
        db.dbQuery {
            // Verify collection belongs to user
            val collection = CollectionTable.selectAll()
                .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
                .singleOrNull() ?: throw NotFoundException("Collection not found")

            CollectionItemTable.deleteWhere { CollectionItemTable.itemId eq itemId }

            // Update collection updatedAt
            CollectionTable.update({ CollectionTable.collectionId eq collectionId }) {
                it[CollectionTable.updatedAt] = timeProvider.nowMillis()
            }
        }
    }

    override suspend fun getCollectionItems(collectionId: String, userId: UUID): List<CollectionItemResponse> = db.dbQuery {
        // Verify collection belongs to user
        val collection = CollectionTable.selectAll()
            .where { (CollectionTable.collectionId eq collectionId) and (CollectionTable.userId eq userId) }
            .singleOrNull() ?: throw NotFoundException("Collection not found")

        CollectionItemTable.selectAll()
            .where { CollectionItemTable.collectionId eq collectionId }
            .orderBy(CollectionItemTable.addedAt, SortOrder.DESC)
            .map { row ->
                CollectionItemResponse(
                    itemId = row[CollectionItemTable.itemId],
                    collectionId = collectionId,
                    itemType = ItemType.valueOf(row[CollectionItemTable.itemType]),
                    itemRefId = row[CollectionItemTable.itemRefId],
                    addedAt = row[CollectionItemTable.addedAt]
                )
            }
    }

    override suspend fun getItemsCount(collectionId: String): Long = db.dbQuery {
        CollectionItemTable.selectAll().where { CollectionItemTable.collectionId eq collectionId }.count()
    }
}

