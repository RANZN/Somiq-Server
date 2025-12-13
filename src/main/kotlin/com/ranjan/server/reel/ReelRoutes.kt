package com.ranjan.server.reel

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.reelRoutes() {
    val reelController by inject<ReelController>()
    routing {
        route("/v1/reels") {
            get { reelController.getReels(call) }
            get("/{id}") { reelController.getReel(call) }

            authenticate(JwtConfig.NAME) {
                post { reelController.createReel(call) }
                put("/{id}") { reelController.updateReel(call) }
                delete("/{id}") { reelController.deleteReel(call) }
                post("/{id}/like") { reelController.toggleLike(call) }
                post("/{id}/bookmark") { reelController.toggleBookmark(call) }
            }
        }
    }
}

