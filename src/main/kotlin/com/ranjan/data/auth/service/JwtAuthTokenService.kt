package com.ranjan.data.auth.service

import com.auth0.jwt.JWT
import com.ranjan.core.config.JwtConfig
import com.ranjan.core.config.JwtConfig.Claims
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.services.AuthTokenProvider
import com.ranjan.domain.common.model.User
import java.util.Date

class JwtAuthTokenService(
    private val timeProvider: TimeProvider,
    private val jwtConfig: JwtConfig,
) : AuthTokenProvider {

    override fun createToken(user: User, deviceId: String): AuthToken {
        val now = timeProvider.nowMillis()
        val userId = user.userId.toString()
        val accessToken = generateAccessToken(userId, user, deviceId, now)
        val refreshToken = generateRefreshToken(userId, deviceId, now)

        return AuthToken(accessToken, refreshToken)
    }

    private fun generateAccessToken(userId: String, user: User, deviceId: String, now: Long): String {
        val issuedAt = Date(now)
        val validity = Date(now + jwtConfig.accessTokenLifetimeMs)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId)
            .withIssuedAt(issuedAt)
            .withClaim(Claims.USER_ID, userId)
            .withClaim(Claims.NAME, user.name)
            .withClaim(Claims.PHONE, user.phone)
            .withClaim(Claims.PUBLIC_USER_ID, user.username)
            .withClaim(Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }

    private fun generateRefreshToken(userId: String, deviceId: String, now: Long): String {
        val issuedAt = Date(now)
        val validity = Date(now + jwtConfig.refreshTokenLifetimeMs)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId)
            .withIssuedAt(issuedAt)
            .withClaim(Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }
}
