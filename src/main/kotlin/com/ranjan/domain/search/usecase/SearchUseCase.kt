package com.ranjan.domain.search.usecase

import com.ranjan.domain.search.model.SearchResult
import com.ranjan.domain.search.repository.SearchRepository

class SearchUseCase(
    private val searchRepository: SearchRepository
) {
    suspend fun execute(
        query: String,
        includeUsers: Boolean = true,
        includePosts: Boolean = true,
        includeReels: Boolean = true,
        limit: Int = 20
    ): Result<SearchResult> = runCatching {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be empty")
        }

        val users = if (includeUsers) searchRepository.searchUsers(query, limit) else emptyList()
        val posts = if (includePosts) searchRepository.searchPosts(query, limit) else emptyList()
        val reels = if (includeReels) searchRepository.searchReels(query, limit) else emptyList()

        SearchResult(users, posts, reels)
    }
}

