package com.ranjan.domain.story.usecase

import com.ranjan.domain.story.repository.StoryRepository
import java.util.UUID

class DeleteStoryUseCase(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(userId: UUID, storyId: String): Result<Unit> = runCatching {
        storyRepository.deleteStory(storyId, userId)
    }
}

