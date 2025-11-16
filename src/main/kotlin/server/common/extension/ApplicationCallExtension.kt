package com.ranjan.server.common.extension

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.userId(): UUID {
    val principal = this.principal<JWTPrincipal>()
        ?: throw IllegalStateException("Invalid or missing authentication token")

    val id = principal.payload
        .getClaim(JwtConfig.Claims.USER_ID)
        .asString()
        ?: throw IllegalStateException("Invalid authentication token")

    return try {
        UUID.fromString(id)
    } catch (_: IllegalArgumentException) {
        throw IllegalStateException("Invalid user ID in token")
    }
}

fun ApplicationCall.userIdOrNull(): UUID? {
    val principal = this.principal<JWTPrincipal>() ?: return null
    val id = principal.payload.getClaim(JwtConfig.Claims.USER_ID).asString()
    return try {
        UUID.fromString(id)
    } catch (_: IllegalArgumentException) {
        null
    }
}
