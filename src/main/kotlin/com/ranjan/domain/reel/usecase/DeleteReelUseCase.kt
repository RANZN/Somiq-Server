package com.ranjan.domain.reel.usecase

import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.reel.repository.ReelRepository
import io.ktor.server.plugins.NotFoundException
import java.util.UUID

class DeleteReelUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID,
        reelId: String
    ): Result<Unit> = runCatching {
        val existing = reelRepository.getReelById(reelId)
            ?: throw NotFoundException("Reel not found")

        if (existing.authorId != userId) {
            throw ForbiddenException("You can only delete your own reels")
        }

        reelRepository.deleteReel(reelId)
    }
}

