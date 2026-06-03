package com.ranjan.core.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

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
}

fun Application.configureSecurity() {
    install(Authentication) {
        jwt(JwtConfig.NAME) {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfig.SECRET))
                    .withAudience(JwtConfig.AUDIENCE)
                    .withIssuer(JwtConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim(JwtConfig.Claims.USER_ID).asString()
                if (userId != null) JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}
