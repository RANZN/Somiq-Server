package com.ranjan.domain.reel.usecase

import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.repository.ReelRepository
import java.util.UUID

class ToggleReelBookmarkUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID,
        reelId: String
    ): Result<ReelResponse> = runCatching {
        reelRepository.toggleBookmark(userId, reelId)
    }
}

