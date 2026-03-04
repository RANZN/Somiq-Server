package com.ranjan.domain.story.usecase

import com.ranjan.domain.story.model.StoryResponse
import com.ranjan.domain.story.repository.StoryRepository
import java.util.UUID

class GetStoryUseCase(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(storyId: String, viewerId: UUID?): Result<StoryResponse?> = runCatching {
        storyRepository.getStoryById(storyId, viewerId)
    }
}
