package com.ranjan.domain.search.model

import com.ranjan.domain.common.model.UserResponse
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.reel.model.ReelResponse
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val users: List<UserResponse>,
    val posts: List<PostResponse>,
    val reels: List<ReelResponse>
)

