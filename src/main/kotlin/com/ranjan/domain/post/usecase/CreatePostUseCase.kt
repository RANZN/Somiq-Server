package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository
import java.util.UUID

class CreatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: UUID,
        caption: String,
        mediaUrls: List<String>
    ): Result<PostResponse> = runCatching {

        if (caption.isBlank() && mediaUrls.isEmpty()) {
            throw IllegalArgumentException("Post must contain either a caption or media")
        }
        postRepository.createPost(userId, caption, mediaUrls)
    }
}
