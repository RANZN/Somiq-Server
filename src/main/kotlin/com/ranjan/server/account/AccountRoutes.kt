package com.ranjan.server.account

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.accountRoutes() {
    val accountController by inject<AccountController>()
    routing {
        route("/v1/account") {
            get { accountController.getProfile(call) }
            get("/{userId}") { accountController.getProfile(call) }

            authenticate(JwtConfig.NAME) {
                put("/profile") { accountController.updateProfile(call) }
                post("/follow/{userId}") { accountController.toggleFollow(call) }
            }
        }
    }
}

