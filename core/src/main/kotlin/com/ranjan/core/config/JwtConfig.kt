package com.ranjan.core.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.ranjan.core.config.serializer.ReadableDurationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration

/** JWT settings, read from the `jwt` section of application.yaml. */
@Serializable
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val signupAudience: String,
    @Serializable(with = ReadableDurationSerializer::class)
    val accessTokenLifetime: Duration,
    @Serializable(with = ReadableDurationSerializer::class)
    val refreshTokenLifetime: Duration,
    @Serializable(with = ReadableDurationSerializer::class)
    val signupTokenLifetime: Duration
) {
    init {
        require(secret.isNotBlank()) { "JWT secret must not be blank (set JWT_SECRET)" }
        require(issuer.isNotBlank()) { "JWT issuer must not be blank" }
        require(audience.isNotBlank()) { "JWT audience must not be blank" }
        require(signupAudience.isNotBlank()) { "JWT signupAudience must not be blank" }
        require(accessTokenLifetime.isPositive()) { "JWT accessTokenLifetime must be positive" }
        require(refreshTokenLifetime.isPositive()) { "JWT refreshTokenLifetime must be positive" }
        require(signupTokenLifetime.isPositive()) { "JWT signupTokenLifetime must be positive" }
    }

    @Transient
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    @Transient
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    @Transient
    val signupVerifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(signupAudience)
        .withClaim(Claims.PURPOSE, Claims.SIGNUP_PURPOSE)
        .build()

    val accessTokenLifetimeMs: Long get() = accessTokenLifetime.inWholeMilliseconds
    val refreshTokenLifetimeMs: Long get() = refreshTokenLifetime.inWholeMilliseconds
    val signupTokenLifetimeMs: Long get() = signupTokenLifetime.inWholeMilliseconds

    companion object {
        /** Name of the Ktor authentication provider, used by `authenticate(JwtConfig.NAME)`. */
        const val NAME = "auth-jwt"
    }

    object Claims {
        const val USER_ID = "userId"
        const val NAME = "name"
        const val PHONE = "phone"
        const val PUBLIC_USER_ID = "publicUserId"
        const val DEVICE_ID = "deviceId"
        const val PURPOSE = "purpose"
        const val SIGNUP_PURPOSE = "signup"
    }
}
