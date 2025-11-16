package com.ranjan.domain.auth.model

import kotlinx.datetime.Instant

data class RefreshTokenEntity(
    val id: String,
    val userId: String,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant
)