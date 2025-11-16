package com.ranjan.application.post

import com.ranjan.data.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject


fun Application.postRoutes() {
    val postController by inject<PostController>()
    routing {
        route("/v1/posts") {
            get { postController.getPosts(call) }
            get("/{id}") { postController.getPost(call) }

            // Requires Auth
            authenticate(JwtConfig.NAME) {
                post { postController.createPost(call) }
                put("/{id}") { postController.updatePost(call) }
                delete("/{id}") { postController.deletePost(call) }

                // Interactions
                post("/{id}/like") { postController.toggleLike(call) }
                post("/{id}/bookmark") { postController.toggleBookmark(call) }
            }
        }
    }
}