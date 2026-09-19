package com.ranjan.data.auth.service

import com.auth0.jwt.JWT
import com.ranjan.core.config.JwtConfig
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.common.model.User
import java.util.Date

object JwtConfig {
    const val NAME = com.ranjan.core.config.JwtConfig.AUTH_NAME
    val Claims = com.ranjan.core.config.JwtConfig.Claims
    val Lifetime = com.ranjan.core.config.JwtConfig.Lifetime
}

class JwtTokenProvider(
    private val timeProvider: TimeProvider,
    private val jwtConfig: JwtConfig
) : TokenProvider {

    override fun createToken(user: User, deviceId: String): AuthToken {
        val accessToken = generateAccessToken(user, deviceId)
        val refreshToken = generateRefreshToken(user.userId.toString(), deviceId)

        return AuthToken(accessToken, refreshToken)
    }

    private fun generateAccessToken(user: User, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + jwtConfig.accessTokenLifetime.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withClaim(JwtConfig.Claims.USER_ID, user.userId.toString())
            .withClaim(JwtConfig.Claims.NAME, user.name)
            .withClaim(JwtConfig.Claims.PHONE, user.phone)
            .withClaim(JwtConfig.Claims.PUBLIC_USER_ID, user.username)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }

    private fun generateRefreshToken(userId: String, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + jwtConfig.refreshTokenLifetime.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(userId)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }

    override fun getUserIdFromRefreshToken(refreshToken: String): String? {
        return try {
            jwtConfig.verifier.verify(refreshToken).subject
        } catch (_: Exception) {
            null
        }
    }

    override fun getDeviceIdFromRefreshToken(refreshToken: String): String? {
        return try {
            jwtConfig.verifier.verify(refreshToken).getClaim(JwtConfig.Claims.DEVICE_ID).asString()
        } catch (_: Exception) {
            null
        }
    }

    override fun createSignupToken(phone: String, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + jwtConfig.signupTokenLifetime.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.signupAudience)
            .withClaim(JwtConfig.Claims.PURPOSE, JwtConfig.Claims.SIGNUP_PURPOSE)
            .withClaim(JwtConfig.Claims.PHONE, phone)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }

    override fun getPhoneFromSignupToken(token: String): String? {
        return try {
            val jwt = jwtConfig.signupVerifier.verify(token)
            if (jwt.getClaim(JwtConfig.Claims.PURPOSE).asString() != JwtConfig.Claims.SIGNUP_PURPOSE) return null
            jwt.getClaim(JwtConfig.Claims.PHONE).asString()
        } catch (_: Exception) {
            null
        }
    }

    override fun getDeviceIdFromSignupToken(token: String): String? {
        return try {
            val jwt = jwtConfig.signupVerifier.verify(token)
            if (jwt.getClaim(JwtConfig.Claims.PURPOSE).asString() != JwtConfig.Claims.SIGNUP_PURPOSE) return null
            jwt.getClaim(JwtConfig.Claims.DEVICE_ID).asString()
        } catch (_: Exception) {
            null
        }
    }
}