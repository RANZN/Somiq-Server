package com.ranjan.domain.auth.model

data class SignupTokenClaims(
    val phone: String,
    val deviceId: String
)
