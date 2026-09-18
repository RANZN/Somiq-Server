package com.ranjan.server.update

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.checkUpdateRoute() {
    routing {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                "Welcome RANJAN"
            )
        }
        get("/checkUpdate") {
            call.respond(
                HttpStatusCode.OK,
                false
            )
        }
    }
}