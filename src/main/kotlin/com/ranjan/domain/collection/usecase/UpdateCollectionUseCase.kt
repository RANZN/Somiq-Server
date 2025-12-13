package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.model.CollectionResponse
import com.ranjan.domain.collection.model.UpdateCollectionRequest
import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class UpdateCollectionUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, collectionId: String, request: UpdateCollectionRequest): Result<CollectionResponse> = runCatching {
        collectionRepository.updateCollection(collectionId, userId, request.name, request.description)
    }
}

