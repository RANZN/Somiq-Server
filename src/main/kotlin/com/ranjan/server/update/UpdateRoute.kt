package com.ranjan.server.update

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.checkUpdateRoute() {
    routing {
        get("/health") {
            call.respondText("Welcome RANJAN", ContentType.Text.Plain, HttpStatusCode.OK)
        }
        get("/checkUpdate") {
            call.respond(
                HttpStatusCode.OK,
                false
            )
        }
    }
}