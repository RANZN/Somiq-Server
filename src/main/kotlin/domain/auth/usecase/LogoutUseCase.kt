package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.repository.RefreshTokenRepo

class LogoutUseCase(
    private val refreshTokenRepo: RefreshTokenRepo,
) {

    suspend fun execute(token: String): Result<Unit> = runCatching {
        val rowsDeleted = refreshTokenRepo.deleteByToken(token)
        if (rowsDeleted == 0) throw SecurityException("Invalid token")
    }
}