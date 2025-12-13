package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class DeleteCollectionUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, collectionId: String): Result<Unit> = runCatching {
        collectionRepository.deleteCollection(collectionId, userId)
    }
}

