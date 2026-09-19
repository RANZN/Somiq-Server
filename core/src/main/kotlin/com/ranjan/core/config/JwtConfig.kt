package com.ranjan.core.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import kotlin.time.Duration

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val signupAudience: String,
    val realm: String,
    val accessTokenLifetime: Duration,
    val refreshTokenLifetime: Duration,
    val signupTokenLifetime: Duration
) {
    init {
        require(secret.isNotBlank()) { "JWT secret must not be blank" }
        require(issuer.isNotBlank()) { "JWT issuer must not be blank" }
        require(audience.isNotBlank()) { "JWT audience must not be blank" }
        require(signupAudience.isNotBlank()) { "JWT signupAudience must not be blank" }
        require(realm.isNotBlank()) { "JWT realm must not be blank" }
        require(accessTokenLifetime.isPositive()) { "JWT accessTokenLifetime must be positive" }
        require(refreshTokenLifetime.isPositive()) { "JWT refreshTokenLifetime must be positive" }
        require(signupTokenLifetime.isPositive()) { "JWT signupTokenLifetime must be positive" }
    }

    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    val signupVerifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(signupAudience)
        .withClaim(Claims.PURPOSE, Claims.SIGNUP_PURPOSE)
        .build()

    val accessTokenLifetimeMs: Long = accessTokenLifetime.inWholeMilliseconds
    val refreshTokenLifetimeMs: Long = refreshTokenLifetime.inWholeMilliseconds
    val signupTokenLifetimeMs: Long = signupTokenLifetime.inWholeMilliseconds

    companion object {
        val NAME: String get() = Env.jwtAuthName
        val AUTH_NAME: String get() = Env.jwtAuthName

        fun get(): JwtConfig = JwtConfig(
            secret = Env.jwtSecret,
            issuer = Env.jwtIssuer,
            audience = Env.jwtAudience,
            signupAudience = Env.jwtSignupAudience,
            realm = Env.jwtAuthName,
            accessTokenLifetime = Env.jwtAccessTokenLifetime,
            refreshTokenLifetime = Env.jwtRefreshTokenLifetime,
            signupTokenLifetime = Env.jwtSignupTokenLifetime
        )
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
