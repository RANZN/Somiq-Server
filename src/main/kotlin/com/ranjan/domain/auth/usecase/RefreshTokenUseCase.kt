package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.auth.repository.UserRepository
import java.util.UUID

class RefreshTokenUseCase(
    private val refreshTokenRepo: RefreshTokenRepo,
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
) {

    suspend fun execute(refreshToken: String): Result<AuthToken> = runCatching {
        val userId = tokenProvider.getUserIdFromRefreshToken(refreshToken)
            ?: throw SecurityException("Invalid or expired refresh token")

        if (!refreshTokenRepo.findByToken(userId, refreshToken)) {
            throw SecurityException("Refresh token not found or revoked")
        }

        val user = userRepository.findById(UUID.fromString(userId))
            ?: throw SecurityException("User not found")

        val newTokens = tokenProvider.createToken(user)

        refreshTokenRepo.deleteByToken(refreshToken)
        refreshTokenRepo.save(userId, newTokens.refreshToken)

        newTokens
    }
}
