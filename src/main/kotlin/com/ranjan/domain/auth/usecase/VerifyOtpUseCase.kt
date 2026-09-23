package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.model.OtpConfig
import com.ranjan.domain.auth.model.OtpVerifyStatus
import com.ranjan.domain.auth.model.VerifyOtpRequest
import com.ranjan.domain.auth.model.VerifyOtpResponse
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.AuthTokenProvider
import com.ranjan.domain.auth.services.SignupTokenService
import com.ranjan.domain.auth.util.normalizePhone

class VerifyOtpUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepo: RefreshTokenRepo,
    private val authTokenProvider: AuthTokenProvider,
    private val signupTokenService: SignupTokenService,
) {

    suspend fun execute(request: VerifyOtpRequest): Result<VerifyOtpResponse> = runCatching {
        val phone = normalizePhone(request.phone)
        if (request.otp.trim() != OtpConfig.DEV_OTP) {
            throw SecurityException("INVALID_OTP")
        }

        val existing = userRepository.findByPhone(phone)
        if (existing != null) {
            val token = authTokenProvider.createToken(existing, request.deviceId)
            refreshTokenRepo.save(existing.userId.toString(), token.refreshToken, request.deviceId)
            VerifyOtpResponse(
                status = OtpVerifyStatus.LOGGED_IN,
                token = token,
                user = existing.asResponse(),
            )
        } else {
            val signupToken = signupTokenService.createSignupToken(phone, request.deviceId)
            VerifyOtpResponse(
                status = OtpVerifyStatus.SIGNUP_REQUIRED,
                signupToken = signupToken,
            )
        }
    }
}
