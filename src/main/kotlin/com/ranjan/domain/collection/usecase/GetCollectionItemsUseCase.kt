package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.model.CollectionItemResponse
import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class GetCollectionItemsUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, collectionId: String): Result<List<CollectionItemResponse>> = runCatching {
        collectionRepository.getCollectionItems(collectionId, userId)
    }
}

