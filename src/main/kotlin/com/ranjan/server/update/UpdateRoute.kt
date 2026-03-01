package com.ranjan.server.update

import com.ranjan.domain.auth.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.checkUpdateRoute() {
    val userRepository by inject<UserRepository>()
    routing {
        route("/"){
            get{
                call.respond(
                    HttpStatusCode.OK,
                    "Welcome RANJAN"
                )
            }
        }
        route("/checkUpdate") {
            get {
                userRepository.isEmailExists("") // waking database to reduce latency for first auth request
                call.respond(
                    HttpStatusCode.OK,
                    false
                )
            }
        }
    }
}