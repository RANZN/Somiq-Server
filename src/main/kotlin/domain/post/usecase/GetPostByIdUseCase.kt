package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException

class GetPostByIdUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(postId: String): Result<PostResponse> =
        runCatching {
            val post = postRepository.getPostById(postId)
                ?: throw NotFoundException("Post not found")

            post
        }
}
