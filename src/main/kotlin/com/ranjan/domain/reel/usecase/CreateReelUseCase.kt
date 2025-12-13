package com.ranjan.domain.reel.usecase

import com.ranjan.domain.reel.model.CreateReelRequest
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.repository.ReelRepository
import java.util.UUID

class CreateReelUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID,
        request: CreateReelRequest
    ): Result<ReelResponse> = runCatching {
        if (request.videoUrl.isBlank()) {
            throw IllegalArgumentException("Video URL cannot be empty")
        }
        reelRepository.createReel(userId, request)
    }
}

