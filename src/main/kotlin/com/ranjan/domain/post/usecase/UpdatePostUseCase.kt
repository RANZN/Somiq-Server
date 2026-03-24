package com.ranjan.domain.post.usecase

import com.ranjan.domain.exception.ForbiddenException
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.UpdatePostRequest
import com.ranjan.domain.post.repository.PostRepository
import com.ranjan.domain.exception.ResourceNotFoundException
import java.util.UUID

class UpdatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: UUID,
        postId: String,
        request: UpdatePostRequest
    ): Result<PostResponse> = runCatching {

        val post = postRepository.getPostById(postId)
            ?: throw ResourceNotFoundException("Post not found")

        if (post.authorId != userId) {
            throw ForbiddenException("You cannot edit this post")
        }

        postRepository.updatePost(postId, request)
    }
}
