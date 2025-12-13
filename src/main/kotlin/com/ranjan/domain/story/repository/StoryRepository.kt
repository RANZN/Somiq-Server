package com.ranjan.domain.story.repository

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.story.model.StoryResponse
import java.util.UUID

interface StoryRepository {
    suspend fun createStory(userId: UUID, mediaUrl: String, mediaType: String): StoryResponse
    suspend fun getStoriesFeed(userId: UUID?, pagination: PaginationRequest): PaginationResult<StoryResponse>
    suspend fun getUserStories(userId: UUID, viewerId: UUID?): List<StoryResponse>
    suspend fun getStoryById(storyId: String, viewerId: UUID?): StoryResponse?
    suspend fun deleteStory(storyId: String, userId: UUID)
    suspend fun recordView(storyId: String, userId: UUID)
    suspend fun deleteExpiredStories()
}

