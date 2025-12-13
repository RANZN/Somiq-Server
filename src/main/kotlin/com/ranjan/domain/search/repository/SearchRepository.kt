package com.ranjan.domain.search.repository

import com.ranjan.domain.common.model.UserResponse
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.reel.model.ReelResponse

interface SearchRepository {
    suspend fun searchUsers(query: String, limit: Int = 20): List<UserResponse>
    suspend fun searchPosts(query: String, limit: Int = 20): List<PostResponse>
    suspend fun searchReels(query: String, limit: Int = 20): List<ReelResponse>
}

