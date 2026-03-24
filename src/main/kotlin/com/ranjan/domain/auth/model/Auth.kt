package com.ranjan.domain.auth.model

import com.ranjan.domain.common.model.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
    val deviceId: String,
)

@Serializable
enum class OtpVerifyStatus {
    LOGGED_IN,
    SIGNUP_REQUIRED,
}

@Serializable
data class VerifyOtpResponse(
    val status: OtpVerifyStatus,
    val token: AuthToken? = null,
    val user: UserResponse? = null,
    val signupToken: String? = null,
)

@Serializable
data class CompleteSignupRequest(
    val name: String,
    /** Public handle (username); must be unique. */
    val userId: String,
    val email: String? = null,
    val profilePictureUrl: String? = null,
)

@Serializable
data class CheckUserIdRequest(
    val userId: String,
)

@Serializable
data class CheckUserIdResponse(
    val available: Boolean,
)

@Serializable
data class AuthResponse(
    val token: AuthToken,
    val user: UserResponse
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val message: String
)

/** Optional dev bypass code; see `otp.devBypassCode` in server config. */
object OtpConfig {
    const val DEV_OTP = "000000"
}