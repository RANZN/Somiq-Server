package com.ranjan.domain.account.usecase

import com.ranjan.domain.account.model.ProfileResponse
import com.ranjan.domain.account.repository.AccountRepository
import com.ranjan.core.exception.ResourceNotFoundException
import java.util.UUID

class GetProfileUseCase(
    private val accountRepository: AccountRepository
) {
    suspend fun execute(userId: UUID, viewerId: UUID?): Result<ProfileResponse> = runCatching {
        accountRepository.getProfile(userId, viewerId) ?: throw ResourceNotFoundException("User not found")
    }
}

