package com.ranjan.server.media

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File

fun Application.mediaRoutes() {
    val mediaController = MediaController
    routing {
        route("/v1/media") {
            authenticate(JwtConfig.NAME) {
                post("/upload") { mediaController.uploadMedia(call) }
            }
        }
        // Serve uploaded files (no auth required for reads)
        staticFiles("/uploads", File("uploads"))
    }
}
