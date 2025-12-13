package com.ranjan.server.comment

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

fun Application.commentRoutes() {
    val commentController by inject<CommentController>()
    routing {
        route("/v1/comments") {
            get { commentController.getComments(call) }

            authenticate(JwtConfig.NAME) {
                post { commentController.createComment(call) }
                put("/{commentId}") { commentController.updateComment(call) }
                delete("/{commentId}") { commentController.deleteComment(call) }
                post("/{commentId}/like") { commentController.toggleLike(call) }
            }
        }
    }
}

