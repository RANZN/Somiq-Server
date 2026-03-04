package com.ranjan.domain.post.usecase

import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.repository.PostRepository
import java.util.*

class GetPostsByAuthorUseCase(
    private val postRepository: PostRepository
) {
    suspend fun execute(
        authorId: UUID,
        viewerId: UUID?,
        pagination: PaginationRequest
    ): Result<PaginationResult<PostResponse>> = runCatching {
        postRepository.getPostsByAuthor(authorId, viewerId, pagination)
    }
}
