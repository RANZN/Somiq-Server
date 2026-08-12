package com.ranjan.server.common.extension

import com.ranjan.data.auth.service.JwtConfig
import com.ranjan.core.exception.InvalidUserIdException
import com.ranjan.core.exception.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.http.content.PartData

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

fun ApplicationCall.baseUrl(): String {
    val headers = request.headers

    val scheme = headers["X-Forwarded-Proto"] ?: request.origin.scheme
    val host = headers["X-Forwarded-Host"] ?: request.host()
    val forwardedPort = headers["X-Forwarded-Port"]?.toIntOrNull()

    val defaultPort = if (scheme.equals("https", ignoreCase = true)) 443 else 80

    return buildString {
        append(scheme)
        append("://")
        append(host)
        forwardedPort
            ?.takeUnless { it == defaultPort }
            ?.let { append(":$it") }
    }
}

fun PartData.FileItem.getExtension(): String {
    val originalName = this.originalFileName
    return if (originalName != null && originalName.contains('.')) {
        originalName.substringAfterLast('.').lowercase()
    } else {
        val contentType = this.contentType
        if (contentType != null) {
            val subtype = contentType.contentSubtype.lowercase()
            when (contentType.contentType.lowercase()) {
                "video" -> if (subtype == "quicktime") "mov" else subtype.takeIf { it.isNotEmpty() } ?: "mp4"
                "image" -> if (subtype == "jpeg") "jpg" else subtype.takeIf { it.isNotEmpty() } ?: "jpg"
                else -> subtype.takeIf { it.isNotEmpty() } ?: "jpg"
            }
        } else {
            "jpg"
        }
    }
}
