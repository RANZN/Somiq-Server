package com.ranjan.core.plugin

import com.ranjan.core.exception.ForbiddenException
import com.ranjan.core.exception.InvalidUserIdException
import com.ranjan.core.exception.ResourceNotFoundException
import com.ranjan.core.exception.UnauthorizedException
import com.ranjan.core.exception.ValidationException
import com.ranjan.core.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

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
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message ?: "Internal server error")
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("Page Not Found"))
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, ErrorResponse("Unauthorized"))
        }
    }
}
