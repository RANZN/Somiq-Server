package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.model.AuthResponse
import com.ranjan.domain.auth.model.LoginRequest
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.common.services.TokenProvider
import com.ranjan.domain.common.services.PasswordCipher

class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepo: RefreshTokenRepo,
    private val passwordCipher: PasswordCipher,
    private val tokenProvider: TokenProvider,
) {

    suspend fun execute(loginRequest: LoginRequest): Result<AuthResponse> = runCatching {
        val user = userRepository.findByEmail(loginRequest.email)
            ?: throw SecurityException("Invalid email or password")

        val isPasswordCorrect = passwordCipher.verifyPassword(loginRequest.password, user.hashedPassword)

        if (isPasswordCorrect.not()) throw SecurityException("Invalid email or password")

        val token = tokenProvider.createToken(user)
        refreshTokenRepo.save(user.userId.toString(), token.refreshToken)
        AuthResponse(token, user.asResponse())
    }
}