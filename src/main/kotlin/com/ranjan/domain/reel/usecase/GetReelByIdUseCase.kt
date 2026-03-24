package com.ranjan.domain.reel.usecase

import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.repository.ReelRepository
import com.ranjan.domain.exception.ResourceNotFoundException

class GetReelByIdUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(reelId: String): Result<ReelResponse> = runCatching {
        reelRepository.getReelById(reelId) ?: throw ResourceNotFoundException("Reel not found")
    }
}

