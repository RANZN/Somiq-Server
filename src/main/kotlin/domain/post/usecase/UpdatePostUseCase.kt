package com.ranjan.domain.post.usecase

import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.UpdatePostRequest
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException
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
            ?: throw NotFoundException("Post not found")

        if (post.authorId != userId) {
            throw ForbiddenException("You cannot edit this post")
        }

        postRepository.updatePost(postId, request)
    }
}
