package com.ranjan.domain.post.model

import com.ranjan.core.util.UUIDSerializer
import com.ranjan.domain.common.model.PaginationResult
import kotlinx.serialization.Serializable
import com.ranjan.core.storage.MediaStorageService
import java.util.UUID

@Serializable
data class PostResponse(
    val postId: String,
    val caption: String,
    @Serializable(with = UUIDSerializer::class)
    val authorId: UUID,
    val authorName: String,
    val authorUsername: String?,
    val authorProfilePictureUrl: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val mediaUrls: List<String>,

    val likesCount: Long,
    val bookmarksCount: Long,

    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

fun PostResponse.withAbsoluteUrls(baseUrl: String, mediaStorageService: MediaStorageService): PostResponse {
    return this.copy(
        mediaUrls = this.mediaUrls.map { relativeUrl ->
            mediaStorageService.getUrlForRelativePath(baseUrl, relativeUrl)
        }
    )
}

fun PaginationResult<PostResponse>.withAbsoluteUrls(baseUrl: String, mediaStorageService: MediaStorageService): PaginationResult<PostResponse> {
    return this.copy(
        data = this.data.map { it.withAbsoluteUrls(baseUrl, mediaStorageService) }
    )
}
