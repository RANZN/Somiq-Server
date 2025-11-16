package com.ranjan.domain.post.usecase

import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.UpdatePostRequest
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException
import java.nio.file.AccessDeniedException

class UpdatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: String,
        postId: String,
        request: UpdatePostRequest
    ): Result<PostResponse> = runCatching {

        val post = postRepository.getPostById(postId)
            ?: throw NotFoundException("Post not found")

        if (post.authorId != userId) {
            throw AccessDeniedException("You cannot edit this post")
        }

        postRepository.updatePost(postId, request)
    }
}
