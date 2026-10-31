package com.ranjan.server.system

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.systemRoutes() {
    routing {
        get("/") {
            call.respondText("Welcome RANJAN", ContentType.Text.Plain, HttpStatusCode.OK)
        }
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
        }
        get("/checkUpdate") {
            call.respond(
                HttpStatusCode.OK,
                false
            )
        }
    }
}