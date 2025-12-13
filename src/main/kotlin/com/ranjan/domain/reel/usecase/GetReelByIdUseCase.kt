package com.ranjan.domain.reel.usecase

import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.repository.ReelRepository
import io.ktor.server.plugins.NotFoundException

class GetReelByIdUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(reelId: String): Result<ReelResponse> = runCatching {
        reelRepository.getReelById(reelId) ?: throw NotFoundException("Reel not found")
    }
}

