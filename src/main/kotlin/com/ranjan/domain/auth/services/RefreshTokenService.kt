package com.ranjan.domain.auth.services

import com.ranjan.domain.auth.model.RefreshTokenClaims

/**
 * Service responsible for validating and parsing refresh tokens.
 */
interface RefreshTokenService {
    fun parseRefreshToken(refreshToken: String): RefreshTokenClaims?
}
