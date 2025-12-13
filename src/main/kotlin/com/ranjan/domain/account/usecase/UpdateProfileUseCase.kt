package com.ranjan.domain.account.usecase

import com.ranjan.domain.account.model.ProfileResponse
import com.ranjan.domain.account.model.UpdateProfileRequest
import com.ranjan.domain.auth.repository.UserRepository
import java.util.UUID

class UpdateProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(
        userId: UUID,
        request: UpdateProfileRequest
    ): Result<ProfileResponse> = runCatching {
        val existing = userRepository.findById(userId) ?: throw IllegalArgumentException("User not found")

        // Check username uniqueness if changing
        if (request.username != null && request.username != existing.username) {
            if (userRepository.isUsernameExists(request.username)) {
                throw IllegalArgumentException("Username already taken")
            }
        }

        val updatedUser = existing.copy(
            name = request.name ?: existing.name,
            username = request.username ?: existing.username,
            bio = request.bio ?: existing.bio,
            profilePictureUrl = request.profilePictureUrl ?: existing.profilePictureUrl
        )

        val saved = userRepository.updateUser(updatedUser) ?: throw Exception("Failed to update profile")
        ProfileResponse(
            user = saved.asResponse(),
            postsCount = 0, // Will be calculated separately if needed
            reelsCount = 0,
            followersCount = 0,
            followingCount = 0,
            isFollowing = false
        )
    }
}

