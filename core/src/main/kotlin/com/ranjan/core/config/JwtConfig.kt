package com.ranjan.core.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class JwtConfig(
    val secret: String,
    val issuer: String = DEFAULT_ISSUER,
    val audience: String = DEFAULT_AUDIENCE,
    val signupAudience: String = DEFAULT_SIGNUP_AUDIENCE,
    val accessTokenLifetime: Duration = 1.hours,
    val refreshTokenLifetime: Duration = 7.days,
    val signupTokenLifetime: Duration = 30.minutes,
    val realm: String = AUTH_NAME
) {
    init {
        require(secret.isNotBlank()) { "JWT secret must not be blank" }
    }

    val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    val signupVerifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(signupAudience)
            .build()
    }

    companion object {
        const val AUTH_NAME = "auth-jwt"
        const val NAME = AUTH_NAME // Backwards compatibility alias

        const val DEFAULT_ISSUER = "somiq-server"
        const val DEFAULT_AUDIENCE = "somiq-app"
        const val DEFAULT_SIGNUP_AUDIENCE = "somiq-signup"
        const val DEV_DEFAULT_SECRET = "DEV_ONLY_CHANGE_IN_PRODUCTION"

        fun fromEnv(env: AppEnv = AppEnv.LOCAL): JwtConfig {
            val rawSecret = System.getenv("JWT_SECRET")
            val secret = when {
                !rawSecret.isNullOrBlank() -> rawSecret
                env.isProduction -> throw IllegalStateException("JWT_SECRET environment variable is required in production!")
                else -> DEV_DEFAULT_SECRET
            }

            return JwtConfig(
                secret = secret,
                issuer = System.getenv("JWT_ISSUER") ?: DEFAULT_ISSUER,
                audience = System.getenv("JWT_AUDIENCE") ?: DEFAULT_AUDIENCE,
                signupAudience = System.getenv("JWT_SIGNUP_AUDIENCE") ?: DEFAULT_SIGNUP_AUDIENCE
            )
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

    object Lifetime {
        val access = 1.hours
        val refresh = 7.days
        val signup = 30.minutes
    }
}
