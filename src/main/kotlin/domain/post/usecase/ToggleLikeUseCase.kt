package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException

class ToggleLikeUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: String,
        postId: String
    ): Result<PostResponse> = runCatching {

        if (!postRepository.exists(postId)) {
            throw NotFoundException("Post not found")
        }

        postRepository.toggleLike(userId, postId)
    }
}
