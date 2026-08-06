package com.ranjan.domain.post.model

import com.ranjan.core.util.UUIDSerializer
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.server.common.extension.baseUrl
import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Serializable
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

fun PostResponse.withAbsoluteUrls(call: ApplicationCall): PostResponse {
    val baseUrl = call.baseUrl()
    return this.copy(
        mediaUrls = this.mediaUrls.map { relativeUrl ->
            if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
                relativeUrl
            } else {
                "$baseUrl/$relativeUrl"
            }
        }
    )
}

fun PaginationResult<PostResponse>.withAbsoluteUrls(call: ApplicationCall): PaginationResult<PostResponse> {
    return this.copy(
        data = this.data.map { it.withAbsoluteUrls(call) }
    )
}
