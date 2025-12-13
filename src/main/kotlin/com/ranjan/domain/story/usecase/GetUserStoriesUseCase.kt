package com.ranjan.domain.story.usecase

import com.ranjan.domain.story.model.StoryResponse
import com.ranjan.domain.story.repository.StoryRepository
import java.util.UUID

class GetUserStoriesUseCase(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(userId: UUID, viewerId: UUID?): Result<List<StoryResponse>> = runCatching {
        storyRepository.getUserStories(userId, viewerId)
    }
}

