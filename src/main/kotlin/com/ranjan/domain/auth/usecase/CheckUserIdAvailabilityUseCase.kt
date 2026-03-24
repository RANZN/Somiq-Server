package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.util.validatePublicUserId

class CheckUserIdAvailabilityUseCase(
    private val userRepository: UserRepository,
) {

    suspend fun execute(userId: String): Result<Boolean> = runCatching {
        validatePublicUserId(userId)
        !userRepository.isUsernameExists(userId)
    }
}
