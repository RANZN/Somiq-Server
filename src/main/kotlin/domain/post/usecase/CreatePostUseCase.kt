package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.CreatePostRequest
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository

class CreatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: String,
        request: CreatePostRequest
    ): Result<PostResponse> = runCatching {

        if (request.content.isBlank()) {
            throw IllegalArgumentException("Post content cannot be empty")
        }

        postRepository.createPost(userId, request)
    }
}
