package com.ranjan.domain.reel.usecase

import com.ranjan.domain.exception.ForbiddenException
import com.ranjan.domain.reel.repository.ReelRepository
import com.ranjan.domain.exception.ResourceNotFoundException
import java.util.UUID

class DeleteReelUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID,
        reelId: String
    ): Result<Unit> = runCatching {
        val existing = reelRepository.getReelById(reelId)
            ?: throw ResourceNotFoundException("Reel not found")

        if (existing.authorId != userId) {
            throw ForbiddenException("You can only delete your own reels")
        }

        reelRepository.deleteReel(reelId)
    }
}

