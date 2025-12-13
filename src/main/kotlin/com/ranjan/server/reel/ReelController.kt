package com.ranjan.server.reel

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.reel.model.*
import com.ranjan.domain.reel.usecase.*
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class ReelController(
    private val createReelUseCase: CreateReelUseCase,
    private val getReelsUseCase: GetReelsUseCase,
    private val getReelByIdUseCase: GetReelByIdUseCase,
    private val updateReelUseCase: UpdateReelUseCase,
    private val deleteReelUseCase: DeleteReelUseCase,
    private val toggleLikeUseCase: ToggleReelLikeUseCase,
    private val toggleBookmarkUseCase: ToggleReelBookmarkUseCase,
    private val recordViewUseCase: RecordReelViewUseCase,
) {

    suspend fun getReels(call: ApplicationCall) {
        val userId = call.userIdOrNull()
        val params = call.request.queryParameters

        val pagination = PaginationRequest(
            cursor = params["after"],
            limit = params["limit"]?.toIntOrNull() ?: 20
        )

        val result = getReelsUseCase.execute(userId, pagination)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load reels")
            )
        }
    }

    suspend fun getReel(call: ApplicationCall) {
        val reelId = call.parameters["id"]
        val userId = call.userIdOrNull()

        if (reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reel id required"))
            return
        }

        // Record view
        recordViewUseCase.execute(userId, reelId)

        val result = getReelByIdUseCase.execute(reelId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Reel not found"))
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to fetch reel")
                    )
            }
        }
    }

    suspend fun createReel(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login first to create reel"))
            return
        }

        val reelRequest = try {
            call.receive<CreateReelRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = createReelUseCase.execute(userId, reelRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ex.message ?: "Failed to create reel")
            )
        }
    }

    suspend fun updateReel(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.message ?: "Unauthorized"))
            return
        }

        val reelId = call.parameters["id"]
        if (reelId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reel id required"))
            return
        }

        val updateRequest = try {
            call.receive<UpdateReelRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = updateReelUseCase.execute(userId, reelId, updateRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Reel not found"))
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to update reel")
                    )
            }
        }
    }

    suspend fun deleteReel(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(e.message ?: "Unauthorized"))
            return
        }

        val reelId = call.parameters["id"]
        if (reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reel id required"))
            return
        }

        val result = deleteReelUseCase.execute(userId, reelId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Reel deleted"))
        }.onFailure { ex ->
            when (ex) {
                is ForbiddenException ->
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not allowed"))
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Reel not found"))
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to delete reel")
                    )
            }
        }
    }

    suspend fun toggleLike(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login to perform Like"))
            return
        }

        val reelId = call.parameters["id"]
        if (reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reel id required"))
            return
        }

        val result = toggleLikeUseCase.execute(userId, reelId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to update like status")
            )
        }
    }

    suspend fun toggleBookmark(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login to Bookmark reels"))
            return
        }

        val reelId = call.parameters["id"]
        if (reelId == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reel id required"))
            return
        }

        val result = toggleBookmarkUseCase.execute(userId, reelId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to update bookmark status")
            )
        }
    }
}

