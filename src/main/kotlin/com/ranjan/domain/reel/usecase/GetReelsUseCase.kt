package com.ranjan.domain.reel.usecase

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.repository.ReelRepository
import java.util.UUID

class GetReelsUseCase(
    private val reelRepository: ReelRepository
) {
    suspend fun execute(
        userId: UUID?,
        pagination: PaginationRequest
    ): Result<PaginationResult<ReelResponse>> = runCatching {
        reelRepository.getReels(userId, pagination)
    }
}

