package com.ranjan.server.auth

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.authRoutes() {
    val authController by inject<AuthController>()
    routing {
        route("/auth") {
            post("/verify-otp") {
                authController.verifyOtp(call)
            }
            post("/complete-signup") {
                authController.completeSignup(call)
            }
            post("/check-user-id") {
                authController.checkUserId(call)
            }
            post("/refresh") {
                authController.refresh(call)
            }
            authenticate(JwtConfig.NAME) {
                post("/logout") {
                    authController.logout(call)
                }
            }
        }
    }
}