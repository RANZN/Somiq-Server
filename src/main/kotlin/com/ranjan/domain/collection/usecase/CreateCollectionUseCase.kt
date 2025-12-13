package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.model.CollectionResponse
import com.ranjan.domain.collection.model.CreateCollectionRequest
import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class CreateCollectionUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID, request: CreateCollectionRequest): Result<CollectionResponse> = runCatching {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Collection name cannot be empty")
        }
        collectionRepository.createCollection(userId, request.name, request.description)
    }
}

