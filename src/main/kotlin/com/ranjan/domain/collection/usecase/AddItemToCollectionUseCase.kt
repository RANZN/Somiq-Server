package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.model.AddItemRequest
import com.ranjan.domain.collection.model.CollectionItemResponse
import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class AddItemToCollectionUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, collectionId: String, request: AddItemRequest): Result<CollectionItemResponse> = runCatching {
        collectionRepository.addItem(collectionId, userId, request.itemType.name, request.itemId)
    }
}

