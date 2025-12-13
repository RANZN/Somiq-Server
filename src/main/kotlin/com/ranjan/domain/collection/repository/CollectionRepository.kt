package com.ranjan.domain.collection.repository

import com.ranjan.domain.collection.model.*
import java.util.UUID

interface CollectionRepository {
    suspend fun createCollection(userId: UUID, name: String, description: String?): CollectionResponse
    suspend fun getCollections(userId: UUID): List<CollectionResponse>
    suspend fun getCollectionById(collectionId: String, userId: UUID): CollectionResponse?
    suspend fun updateCollection(collectionId: String, userId: UUID, name: String?, description: String?): CollectionResponse
    suspend fun deleteCollection(collectionId: String, userId: UUID)
    suspend fun addItem(collectionId: String, userId: UUID, itemType: String, itemRefId: String): CollectionItemResponse
    suspend fun removeItem(collectionId: String, itemId: String, userId: UUID)
    suspend fun getCollectionItems(collectionId: String, userId: UUID): List<CollectionItemResponse>
    suspend fun getItemsCount(collectionId: String): Long
}

