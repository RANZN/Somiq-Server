package com.ranjan.domain.account.model

import com.ranjan.domain.common.model.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val user: UserResponse,
    val postsCount: Long = 0,
    val reelsCount: Long = 0,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val isFollowing: Boolean = false
)

