package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.AuthTokenProvider
import com.ranjan.domain.auth.services.RefreshTokenService
import java.util.UUID

class RefreshTokenUseCase(
    private val refreshTokenRepo: RefreshTokenRepo,
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val authTokenProvider: AuthTokenProvider,
) {

    suspend fun execute(refreshToken: String): Result<AuthToken> = runCatching {
        val claims = refreshTokenService.parseRefreshToken(refreshToken)
            ?: throw SecurityException("Invalid or expired refresh token")
        val userId = claims.userId
        val deviceId = claims.deviceId

        if (!refreshTokenRepo.findByToken(userId, refreshToken, deviceId)) {
            throw SecurityException("Unauthorized device for refresh token")
        }

        val user = userRepository.findById(UUID.fromString(userId))
            ?: throw SecurityException("User not found")

        val newTokens = authTokenProvider.createToken(user, deviceId)

        refreshTokenRepo.deleteByToken(refreshToken)
        refreshTokenRepo.save(userId, newTokens.refreshToken, deviceId)

        newTokens
    }
}
