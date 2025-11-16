package com.ranjan.domain.post.repository

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.post.model.CreatePostRequest
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.UpdatePostRequest

interface PostRepository {

    suspend fun createPost(
        userId: String,
        request: CreatePostRequest
    ): PostResponse

    suspend fun getPosts(
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
        userId: String,
        postId: String
    ): PostResponse

    suspend fun toggleBookmark(
        userId: String,
        postId: String
    ): PostResponse
}
