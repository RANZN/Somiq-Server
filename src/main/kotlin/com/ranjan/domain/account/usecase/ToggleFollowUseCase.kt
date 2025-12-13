package com.ranjan.domain.account.usecase

import com.ranjan.domain.account.repository.AccountRepository
import java.util.UUID

class ToggleFollowUseCase(
    private val accountRepository: AccountRepository
) {
    suspend fun execute(
        followerId: UUID,
        followingId: UUID
    ): Result<Boolean> = runCatching {
        accountRepository.toggleFollow(followerId, followingId)
    }
}

