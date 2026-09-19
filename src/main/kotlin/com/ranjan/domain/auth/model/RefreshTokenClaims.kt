package com.ranjan.domain.auth.model

data class RefreshTokenClaims(
    val userId: String,
    val deviceId: String
)
