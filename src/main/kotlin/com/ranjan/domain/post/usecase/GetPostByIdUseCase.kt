package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository
import com.ranjan.core.exception.ResourceNotFoundException

class GetPostByIdUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(postId: String): Result<PostResponse> =
        runCatching {
            val post = postRepository.getPostById(postId)
                ?: throw ResourceNotFoundException("Post not found")

            post
        }
}
