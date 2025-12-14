package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.ToggleResponse
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException
import java.util.UUID

class ToggleBookmarkUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: UUID,
        postId: String
    ): Result<ToggleResponse> = runCatching {

        if (!postRepository.exists(postId)) {
            throw NotFoundException("Post not found")
        }

        postRepository.toggleBookmark(userId, postId)
    }
}
