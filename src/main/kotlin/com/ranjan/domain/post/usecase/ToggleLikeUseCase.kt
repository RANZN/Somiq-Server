package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.ToggleResponse
import com.ranjan.domain.post.repository.PostRepository
import com.ranjan.core.exception.ResourceNotFoundException
import java.util.UUID

class ToggleLikeUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: UUID,
        postId: String
    ): Result<ToggleResponse> = runCatching {

        if (!postRepository.exists(postId)) {
            throw ResourceNotFoundException("Post not found")
        }

        postRepository.toggleLike(userId, postId)
    }
}
