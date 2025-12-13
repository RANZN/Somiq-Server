package com.ranjan.domain.collection.usecase

import com.ranjan.domain.collection.model.CollectionResponse
import com.ranjan.domain.collection.repository.CollectionRepository
import java.util.UUID

class GetCollectionsUseCase(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(userId: UUID): Result<List<CollectionResponse>> = runCatching {
        collectionRepository.getCollections(userId)
    }
}

