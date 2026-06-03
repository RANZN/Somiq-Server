package com.ranjan.core.config

import com.ranjan.core.exception.ForbiddenException
import com.ranjan.core.exception.InvalidUserIdException
import com.ranjan.core.exception.ResourceNotFoundException
import com.ranjan.core.exception.UnauthorizedException
import com.ranjan.core.exception.ValidationException
import com.ranjan.core.model.ErrorResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<InvalidUserIdException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
        }

        exception<UnauthorizedException> { call, _ ->
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
        }

        exception<ResourceNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(cause.message ?: "Not found")
            )
        }

        exception<ForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(cause.message ?: "Forbidden")
            )
        }

        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(cause.message ?: "Invalid request")
            )
        }

        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: ${cause.message}",
                status = HttpStatusCode.InternalServerError
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "404: Page Not Found", status = status)
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respondText(text = "401: Unauthorized", status = status)
        }
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() //todo: In production, we should restrict this
    }
}
