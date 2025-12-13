package com.ranjan.server.account

import com.ranjan.domain.account.model.UpdateProfileRequest
import com.ranjan.domain.account.usecase.GetProfileUseCase
import com.ranjan.domain.account.usecase.ToggleFollowUseCase
import com.ranjan.domain.account.usecase.UpdateProfileUseCase
import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.server.common.extension.userId
import com.ranjan.server.common.extension.userIdOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class AccountController(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase
) {

    suspend fun getProfile(call: ApplicationCall) {
        val userIdParam = call.parameters["userId"]
        val viewerId = call.userIdOrNull()

        val userId = if (userIdParam != null) {
            try {
                UUID.fromString(userIdParam)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
                return
            }
        } else {
            // If no userId provided, return current user's profile
            try {
                call.userId()
            } catch (_: Exception) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
                return
            }
        }

        val result = getProfileUseCase.execute(userId, viewerId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            when (ex) {
                is NotFoundException ->
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(ex.message ?: "Failed to fetch profile")
                    )
            }
        }
    }

    suspend fun updateProfile(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val updateRequest = try {
            call.receive<UpdateProfileRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = updateProfileUseCase.execute(userId, updateRequest)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to update profile")
            )
        }
    }

    suspend fun toggleFollow(call: ApplicationCall) {
        val followerId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val followingIdParam = call.parameters["userId"]
        if (followingIdParam == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("User ID required"))
            return
        }

        val followingId = try {
            UUID.fromString(followingIdParam)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
            return
        }

        val result = toggleFollowUseCase.execute(followerId, followingId)

        result.onSuccess { isFollowing ->
            call.respond(
                HttpStatusCode.OK,
                mapOf("isFollowing" to isFollowing, "message" to if (isFollowing) "Followed" else "Unfollowed")
            )
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to toggle follow")
            )
        }
    }
}

