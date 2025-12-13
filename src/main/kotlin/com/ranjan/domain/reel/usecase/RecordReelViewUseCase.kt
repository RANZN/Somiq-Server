package com.ranjan.domain.reel.usecase

import com.ranjan.domain.reel.repository.ReelRepository
import java.util.UUID

class RecordReelViewUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID?,
        reelId: String
    ): Result<Unit> = runCatching {
        reelRepository.recordView(userId, reelId)
    }
}

