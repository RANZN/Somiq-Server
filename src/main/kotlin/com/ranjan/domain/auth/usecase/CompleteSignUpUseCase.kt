package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.model.AuthResponse
import com.ranjan.domain.auth.model.CompleteSignupRequest
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.AuthTokenProvider
import com.ranjan.domain.auth.services.SignupTokenService
import com.ranjan.domain.auth.util.validatePublicUserId
import com.ranjan.domain.common.model.User
import java.util.UUID

class CompleteSignUpUseCase(
    private val userRepository: UserRepository,
    private val authTokenProvider: AuthTokenProvider,
    private val signupTokenService: SignupTokenService,
    private val refreshTokenRepo: RefreshTokenRepo,
) {

    suspend fun execute(signupToken: String, request: CompleteSignupRequest): Result<AuthResponse> = runCatching {
        val claims = signupTokenService.parseSignupToken(signupToken)
            ?: throw SecurityException("INVALID_SIGNUP_TOKEN")
        val phone = claims.phone
        val deviceId = claims.deviceId

        if (request.name.isBlank()) {
            throw IllegalArgumentException("NAME_REQUIRED")
        }

        validatePublicUserId(request.userId)

        if (!request.email.isNullOrBlank() && !request.email.contains("@")) {
            throw IllegalArgumentException("EMAIL_INVALID")
        }

        if (userRepository.isPhoneExists(phone)) {
            throw IllegalStateException("PHONE_ALREADY_IN_USE")
        }

        if (userRepository.isUsernameExists(request.userId)) {
            throw IllegalStateException("USERNAME_ALREADY_IN_USE")
        }

        request.email?.takeIf { it.isNotBlank() }?.let { email ->
            if (userRepository.isEmailExists(email)) {
                throw IllegalStateException("EMAIL_ALREADY_IN_USE")
            }
        }

        val newUser = User(
            userId = UUID.randomUUID(),
            name = request.name.trim(),
            email = request.email?.takeIf { it.isNotBlank() },
            phone = phone,
            username = request.userId,
            profilePictureUrl = request.profilePictureUrl?.takeIf { it.isNotBlank() },
        )

        val savedUser = userRepository.saveUser(newUser) ?: throw Exception("Failed to create account")

        val token = authTokenProvider.createToken(savedUser, deviceId)
        refreshTokenRepo.save(savedUser.userId.toString(), token.refreshToken, deviceId)
        AuthResponse(token, user = savedUser.asResponse())
    }
}
