package com.ranjan.server.common.extension

import com.ranjan.data.auth.service.JwtConfig
import com.ranjan.core.exception.InvalidUserIdException
import com.ranjan.core.exception.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

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

fun ApplicationCall.getUserIdAndViewerId(): Pair<UUID, UUID?> {

    val viewerId = userIdOrNull()

    val userId = parameters["userId"]?.let {
        runCatching { UUID.fromString(it) }
            .getOrElse { throw InvalidUserIdException() }
    } ?: runCatching { userId() }
        .getOrElse { throw UnauthorizedException() }

    return userId to viewerId
}
