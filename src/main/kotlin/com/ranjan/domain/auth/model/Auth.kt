package com.ranjan.domain.auth.model

import com.ranjan.domain.common.model.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class AuthResponse(
    val token: AuthToken,
    val user: UserResponse
)

@Serializable
data class ErrorResponse(
    val message: String
)