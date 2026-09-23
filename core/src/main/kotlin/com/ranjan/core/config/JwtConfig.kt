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
        val NAME: String by lazy { Env.load().require("JWT_AUTH_NAME") }

        fun from(env: Env): JwtConfig {
            val secret = env.require("JWT_SECRET")
            val issuer = env.require("JWT_ISSUER")
            val audience = env.require("JWT_AUDIENCE")
            val signupAudience = env.require("JWT_SIGNUP_AUDIENCE")
            val realm = env.require("JWT_AUTH_NAME")
            val accessLifetime = env.duration("JWT_ACCESS_TOKEN_LIFETIME")
            val refreshLifetime = env.duration("JWT_REFRESH_TOKEN_LIFETIME")
            val signupLifetime = env.duration("JWT_SIGNUP_TOKEN_LIFETIME")
            env.validate()
            return JwtConfig(secret, issuer, audience, signupAudience, realm, accessLifetime, refreshLifetime, signupLifetime)
        }
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
