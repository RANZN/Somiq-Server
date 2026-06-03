package com.ranjan.server.story

import com.ranjan.core.model.ErrorResponse
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.story.model.CreateStoryRequest
import com.ranjan.domain.story.usecase.*
import com.ranjan.server.common.extension.getUserIdAndViewerId
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class StoryController(
    private val createStoryUseCase: CreateStoryUseCase,
    private val getStoriesFeedUseCase: GetStoriesFeedUseCase,
    private val getStoryUseCase: GetStoryUseCase,
    private val getUserStoriesUseCase: GetUserStoriesUseCase,
    private val deleteStoryUseCase: DeleteStoryUseCase,
    private val recordStoryViewUseCase: RecordStoryViewUseCase,
) {

    suspend fun getStoriesFeed(call: ApplicationCall) {
        val userId = call.userIdOrNull()
        val params = call.request.queryParameters

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getStoriesFeedUseCase.execute(userId, pagination)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load stories")
            )
        }
    }

    suspend fun getStory(call: ApplicationCall) {
        val storyId = call.parameters["storyId"]
        if (storyId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Story id required"))
            return
        }
        val viewerId = call.userIdOrNull()
        val result = getStoryUseCase.execute(storyId, viewerId)
        result.onSuccess { story ->
            if (story != null) {
                call.respond(HttpStatusCode.OK, story)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Story not found"))
            }
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load story")
            )
        }
    }

    suspend fun getUserStories(call: ApplicationCall) {
        val (userId, viewerId) = call.getUserIdAndViewerId()
        val result = getUserStoriesUseCase.execute(userId, viewerId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load user stories")
            )
        }
    }

    suspend fun createStory(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required to create story"))
            return
        }

        val storyRequest = try {
            call.receive<CreateStoryRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = createStoryUseCase.execute(userId, storyRequest.mediaUrl, storyRequest.mediaType.name)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to create story")
            )
        }
    }

    suspend fun deleteStory(call: ApplicationCall) {
        val userId = call.userIdOrNull() ?: run {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val storyId = call.parameters["storyId"]
        if (storyId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Story id required"))
            return
        }

        val result = deleteStoryUseCase.execute(userId, storyId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Story deleted"))
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to delete story")
            )
        }
    }

    suspend fun recordView(call: ApplicationCall) {
        val userId = call.userIdOrNull() ?: run {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val storyId = call.parameters["storyId"]
        if (storyId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Story id required"))
            return
        }

        val result = recordStoryViewUseCase.execute(userId, storyId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "View recorded"))
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to record view")
            )
        }
    }
}