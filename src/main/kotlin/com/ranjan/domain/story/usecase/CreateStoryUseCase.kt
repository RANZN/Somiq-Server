package com.ranjan.domain.story.usecase

import com.ranjan.domain.story.model.StoryResponse
import com.ranjan.domain.story.repository.StoryRepository
import java.util.UUID

class CreateStoryUseCase(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(userId: UUID, mediaUrl: String, mediaType: String): Result<StoryResponse> = runCatching {
        if (mediaUrl.isBlank()) {
            throw IllegalArgumentException("Media URL cannot be empty")
        }
        storyRepository.createStory(userId, mediaUrl, mediaType)
    }
}

