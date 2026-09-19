package com.ranjan.core.plugin

import com.ranjan.core.config.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(config: JwtConfig) {
    install(Authentication) {
        jwt(config.realm) {
            verifier(config.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim(JwtConfig.Claims.USER_ID).asString()
                if (userId != null) JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}
