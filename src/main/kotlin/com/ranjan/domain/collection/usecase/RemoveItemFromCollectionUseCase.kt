package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class RemoveItemFromCollectionUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, collectionId: String, itemId: String): Result<Unit> = runCatching {
        collectionRepository.removeItem(collectionId, itemId, userId)
    }
}

