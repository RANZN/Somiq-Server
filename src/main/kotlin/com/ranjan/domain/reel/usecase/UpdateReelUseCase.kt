package com.ranjan.domain.reel.usecase

import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.model.UpdateReelRequest
import com.ranjan.domain.reel.repository.ReelRepository
import io.ktor.server.plugins.NotFoundException
import java.util.UUID

class UpdateReelUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID,
        reelId: String,
        request: UpdateReelRequest
    ): Result<ReelResponse> = runCatching {
        val existing = reelRepository.getReelById(reelId)
            ?: throw NotFoundException("Reel not found")

        if (existing.authorId != userId) {
            throw ForbiddenException("You can only update your own reels")
        }

        reelRepository.updateReel(reelId, request)
    }
}

