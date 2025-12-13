package com.ranjan.domain.story.usecase

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.story.model.StoryResponse
import com.ranjan.domain.story.repository.StoryRepository
import java.util.UUID

class GetStoriesFeedUseCase(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(userId: UUID?, pagination: PaginationRequest): Result<PaginationResult<StoryResponse>> = runCatching {
        storyRepository.getStoriesFeed(userId, pagination)
    }
}

