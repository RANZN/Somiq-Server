package com.ranjan.domain.auth.usecase

import com.ranjan.domain.auth.repository.UserRepository

class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
) {

    suspend fun execute(email: String) = runCatching {
        userRepository.findByEmail(email) ?: throw SecurityException("User Not Found")
    }
}