package com.ranjan.domain.reel.repository

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.reel.model.CreateReelRequest
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.model.UpdateReelRequest
import java.util.UUID

interface ReelRepository {
    suspend fun createReel(
        userId: UUID,
        request: CreateReelRequest
    ): ReelResponse

    suspend fun getReels(
        userId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<ReelResponse>

    suspend fun getReelById(id: String): ReelResponse?

    suspend fun updateReel(
        reelId: String,
        request: UpdateReelRequest
    ): ReelResponse

    suspend fun deleteReel(reelId: String)

    suspend fun exists(reelId: String): Boolean

    suspend fun toggleLike(
        userId: UUID,
        reelId: String
    ): ReelResponse

    suspend fun toggleBookmark(
        userId: UUID,
        reelId: String
    ): ReelResponse

    suspend fun recordView(
        userId: UUID?,
        reelId: String
    )
}

