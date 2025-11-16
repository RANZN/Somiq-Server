package com.ranjan.domain.post.model

import java.util.UUID

data class PostResponse(
    val id: String,
    val title: String,
    val content: String,
    val authorId: UUID,
    val createdAt: Long,
    val updatedAt: Long?,
    val mediaUrls: List<String>,

    val likesCount: Long,
    val bookmarksCount: Long,

    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)