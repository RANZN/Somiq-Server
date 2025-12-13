package com.ranjan.server.auth

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.auth.model.ForgotPasswordRequest
import com.ranjan.domain.auth.model.LoginRequest
import com.ranjan.domain.auth.model.ResetPasswordRequest
import com.ranjan.domain.auth.model.SignupRequest
import com.ranjan.domain.auth.usecase.ForgotPasswordUseCase
import com.ranjan.domain.auth.usecase.LoginUserUseCase
import com.ranjan.domain.auth.usecase.LogoutUseCase
import com.ranjan.domain.auth.usecase.SignUpUserUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class AuthController(
    private val loginUserUseCase: LoginUserUseCase,
    private val signupUserUseCase: SignUpUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
) {

    suspend fun login(call: ApplicationCall) {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        val result = loginUserUseCase.execute(loginRequest)

        result.onSuccess { authResponse ->
            call.respond(HttpStatusCode.OK, authResponse)
        }
        result.onFailure { exception ->
            when (exception) {
                is SecurityException -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(exception.message ?: "Invalid credentials")
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

    suspend fun signup(call: ApplicationCall) {
        val signUpRequest = try {
            call.receive<SignupRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        val result = signupUserUseCase.execute(signUpRequest)

        result.onSuccess { authResponse ->
            call.respond(HttpStatusCode.Created, authResponse)
        }

        result.onFailure { exception ->
            when (exception) {
                is SecurityException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(exception.message ?: "Invalid credentials")
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

    suspend fun forgot(call: ApplicationCall) {
        val request = try {
            call.receive<ForgotPasswordRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
        }

        forgotPasswordUseCase.execute(request.email)

        call.respond(
            HttpStatusCode.OK,
            mapOf("message" to "If an account with that email exists, a password reset link has been sent.")
        )
    }

    suspend fun resetPassword(call: ApplicationCall) {
        val request = try {
            call.receive<ResetPasswordRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format.")
            )
            return
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