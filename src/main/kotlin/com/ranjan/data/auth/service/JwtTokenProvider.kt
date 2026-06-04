package com.ranjan.data.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.common.model.User
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object JwtConfig {
    const val NAME = "auth-jwt"

    private fun env(name: String): String? = System.getenv(name)

    /** JWT signing secret. Set JWT_SECRET in production; dev default only when unset. */
    val SECRET: String
        get() = env("JWT_SECRET")
            ?: "DEV_ONLY_CHANGE_IN_PRODUCTION".also {
                System.err.println("WARNING: JWT_SECRET not set. Using dev default. Set JWT_SECRET for production.")
            }

    /** JWT issuer. Override with JWT_ISSUER. */
    val ISSUER: String get() = env("JWT_ISSUER") ?: "somiq-server"

    /** JWT audience. Override with JWT_AUDIENCE. */
    val AUDIENCE: String get() = env("JWT_AUDIENCE") ?: "somiq-app"

    /** Audience for pre-registration signup tokens. */
    const val SIGNUP_AUDIENCE: String = "somiq-signup"

    object Claims {
        const val USER_ID = "userId"
        const val NAME = "name"
        const val PHONE = "phone"
        /** Public handle (same as username in DB). */
        const val PUBLIC_USER_ID = "publicUserId"
        const val DEVICE_ID = "deviceId"
        const val PURPOSE = "purpose"
        const val SIGNUP_PURPOSE = "signup"
    }

    object Lifetime {
        val access = 1.hours
        val refresh = 7.days
        val signup = 30.minutes
    }
}

class JwtTokenProvider(
    private val timeProvider: TimeProvider
) : TokenProvider {

    override fun createToken(user: User, deviceId: String): AuthToken {
        val accessToken = generateAccessToken(user, deviceId)
        val refreshToken = generateRefreshToken(user.userId.toString(), deviceId)

        return AuthToken(accessToken, refreshToken)
    }

    private fun generateAccessToken(user: User, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + JwtConfig.Lifetime.access.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(JwtConfig.ISSUER)
            .withAudience(JwtConfig.AUDIENCE)
            .withClaim(JwtConfig.Claims.USER_ID, user.userId.toString())
            .withClaim(JwtConfig.Claims.NAME, user.name)
            .withClaim(JwtConfig.Claims.PHONE, user.phone)
            .withClaim(JwtConfig.Claims.PUBLIC_USER_ID, user.username)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))
    }

    private fun generateRefreshToken(userId: String, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + JwtConfig.Lifetime.refresh.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(JwtConfig.ISSUER)
            .withAudience(JwtConfig.AUDIENCE)
            .withSubject(userId)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))
    }

    override fun getUserIdFromRefreshToken(refreshToken: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(JwtConfig.SECRET))
                .withIssuer(JwtConfig.ISSUER)
                .withAudience(JwtConfig.AUDIENCE)
                .build()
            verifier.verify(refreshToken).subject
        } catch (_: Exception) {
            null
        }
    }

    override fun getDeviceIdFromRefreshToken(refreshToken: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(JwtConfig.SECRET))
                .withIssuer(JwtConfig.ISSUER)
                .withAudience(JwtConfig.AUDIENCE)
                .build()
            verifier.verify(refreshToken).getClaim(JwtConfig.Claims.DEVICE_ID).asString()
        } catch (_: Exception) {
            null
        }
    }

    override fun createSignupToken(phone: String, deviceId: String): String {
        val validity = Date(timeProvider.nowMillis() + JwtConfig.Lifetime.signup.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(JwtConfig.ISSUER)
            .withAudience(JwtConfig.SIGNUP_AUDIENCE)
            .withClaim(JwtConfig.Claims.PURPOSE, JwtConfig.Claims.SIGNUP_PURPOSE)
            .withClaim(JwtConfig.Claims.PHONE, phone)
            .withClaim(JwtConfig.Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))
    }

    override fun getPhoneFromSignupToken(token: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(JwtConfig.SECRET))
                .withIssuer(JwtConfig.ISSUER)
                .withAudience(JwtConfig.SIGNUP_AUDIENCE)
                .build()
            val jwt = verifier.verify(token)
            if (jwt.getClaim(JwtConfig.Claims.PURPOSE).asString() != JwtConfig.Claims.SIGNUP_PURPOSE) return null
            jwt.getClaim(JwtConfig.Claims.PHONE).asString()
        } catch (_: Exception) {
            null
        }
    }

    override fun getDeviceIdFromSignupToken(token: String): String? {
        return try {
            val verifier = JWT
                .require(Algorithm.HMAC256(JwtConfig.SECRET))
                .withIssuer(JwtConfig.ISSUER)
                .withAudience(JwtConfig.SIGNUP_AUDIENCE)
                .build()
            val jwt = verifier.verify(token)
            if (jwt.getClaim(JwtConfig.Claims.PURPOSE).asString() != JwtConfig.Claims.SIGNUP_PURPOSE) return null
            jwt.getClaim(JwtConfig.Claims.DEVICE_ID).asString()
        } catch (_: Exception) {
            null
        }
    }

}