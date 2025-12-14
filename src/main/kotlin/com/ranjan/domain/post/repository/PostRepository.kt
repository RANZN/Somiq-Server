package com.ranjan.domain.post.repository

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.post.model.CreatePostRequest
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.ToggleResponse
import com.ranjan.domain.post.model.UpdatePostRequest
import java.util.UUID

interface PostRepository {

    suspend fun createPost(
        userId: UUID,
        request: CreatePostRequest
    ): PostResponse

    suspend fun getPosts(
        userId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<PostResponse>

    suspend fun getPostById(id: String): PostResponse?

    suspend fun updatePost(
        postId: String,
        request: UpdatePostRequest
    ): PostResponse

    suspend fun deletePost(postId: String)

    suspend fun exists(postId: String): Boolean

    suspend fun toggleLike(
        userId: UUID,
        postId: String
    ): ToggleResponse

    suspend fun toggleBookmark(
        userId: UUID,
        postId: String
    ): ToggleResponse
}
