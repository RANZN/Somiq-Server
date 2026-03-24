package com.ranjan.server.auth

import com.ranjan.domain.auth.model.*
import com.ranjan.domain.auth.usecase.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthController(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val completeSignUpUseCase: CompleteSignUpUseCase,
    private val checkUserIdAvailabilityUseCase: CheckUserIdAvailabilityUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) {

    suspend fun verifyOtp(call: ApplicationCall) {
        val request = try {
            call.receive<VerifyOtpRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        val result = verifyOtpUseCase.execute(request)

        result.onSuccess { response ->
            call.respond(HttpStatusCode.OK, response)
        }
        result.onFailure { exception ->
            when (exception) {
                is SecurityException -> {
                    val message = exception.message ?: "Invalid credentials"
                    val status = when (message) {
                        "INVALID_OTP" -> HttpStatusCode.Unauthorized
                        "ACCOUNT_NOT_FOUND" -> HttpStatusCode.NotFound
                        else -> HttpStatusCode.Unauthorized
                    }
                    val publicMessage = when (message) {
                        "INVALID_OTP" -> "Incorrect OTP"
                        "ACCOUNT_NOT_FOUND" -> "Account not found"
                        else -> "Invalid credentials"
                    }
                    call.respond(status, ErrorResponse(publicMessage))
                }

                is IllegalStateException -> {
                    val errorMessage = exception.message ?: "Conflict"
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse(errorMessage)
                    )
                }

                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("An internal server error occurred.")
                    )
                }
            }
        }
    }

    suspend fun completeSignup(call: ApplicationCall) {
        val signupToken = call.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            ?: run {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing signup token"))
                return
            }

        val body = try {
            call.receive<CompleteSignupRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        val result = completeSignUpUseCase.execute(signupToken, body)

        result.onSuccess { authResponse ->
            call.respond(HttpStatusCode.Created, authResponse)
        }
        result.onFailure { exception ->
            when (exception) {
                is SecurityException -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(exception.message ?: "Invalid signup token")
                    )
                }

                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(exception.message ?: "Invalid signup data")
                    )
                }

                is IllegalStateException -> {
                    val errorMessage = exception.message ?: "This resource already exists."
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse(errorMessage)
                    )
                }

                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("An internal server error occurred.")
                    )
                }
            }
        }
    }

    suspend fun checkUserId(call: ApplicationCall) {
        val request = try {
            call.receive<CheckUserIdRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        val result = checkUserIdAvailabilityUseCase.execute(request.userId)

        result.onSuccess { available ->
            call.respond(HttpStatusCode.OK, CheckUserIdResponse(available))
        }
        result.onFailure { exception ->
            when (exception) {
                is IllegalArgumentException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(exception.message ?: "Invalid user id")
                    )
                }

                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("An internal server error occurred.")
                    )
                }
            }
        }
    }

    suspend fun refresh(call: ApplicationCall) {
        val request = try {
            call.receive<RefreshTokenRequest>()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format."))
            return
        }

        val result = refreshTokenUseCase.execute(request.refreshToken)

        result.onSuccess { token ->
            call.respond(
                HttpStatusCode.OK,
                RefreshTokenResponse(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                ),
            )
        }.onFailure { exception ->
            when (exception) {
                is SecurityException -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(exception.message ?: "Invalid or expired refresh token"),
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("An internal server error occurred."),
                    )
                }
            }
        }
    }

    suspend fun logout(call: ApplicationCall) {
        val refreshToken = call.request.header("Authorization")?.removePrefix("Bearer ")
            ?: return call.respond(HttpStatusCode.BadRequest, "Missing token")

        val response = logoutUseCase.execute(refreshToken)

        response.onSuccess {
            call.respond(HttpStatusCode.OK, "Logged out successfully")
        }.onFailure {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
        }
    }
}
