package com.ranjan.domain.account.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val profilePictureUrl: String? = null
)

