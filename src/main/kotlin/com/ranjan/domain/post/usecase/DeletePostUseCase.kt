package com.ranjan.domain.post.usecase

import com.ranjan.domain.common.exceptions.ForbiddenException
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.NotFoundException
import java.util.UUID

class DeletePostUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        userId: UUID,
        postId: String
    ): Result<Unit> = runCatching {

        val post = postRepository.getPostById(postId)
            ?: throw NotFoundException("Post not found")

        if (post.authorId != userId) {
            throw ForbiddenException("You cannot delete this post")
        }

        postRepository.deletePost(postId)
    }
}
