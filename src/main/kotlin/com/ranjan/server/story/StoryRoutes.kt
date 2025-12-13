package com.ranjan.server.story

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.storyRoutes() {
    val storyController by inject<StoryController>()
    routing {
        route("/v1/stories") {
            get { storyController.getStoriesFeed(call) }
            get("/user/{userId}") { storyController.getUserStories(call) }
            get("/user") { storyController.getUserStories(call) }

            authenticate(JwtConfig.NAME) {
                post { storyController.createStory(call) }
                delete("/{storyId}") { storyController.deleteStory(call) }
                post("/{storyId}/view") { storyController.recordView(call) }
            }
        }
    }
}

