package com.ranjan.domain.common.model

import com.ranjan.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val name: String,
    val email: String,
    val username: String? = null,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val hashedPassword: String,
) {
    fun asResponse() = UserResponse(
        userId = userId.toString(),
        name = name,
        email = email,
        username = username,
        profilePictureUrl = profilePictureUrl,
        bio = bio
    )
}

@Serializable
data class UserResponse(
    val userId: String,
    val name: String,
    val email: String,
    val username: String? = null,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
)