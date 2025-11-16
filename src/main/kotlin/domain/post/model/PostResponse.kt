package com.ranjan.domain.post.model

data class PostResponse(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val mediaUrls: List<String>,
    val isEdited: Boolean
)

fun Post.toResponse() = PostResponse(
    id = id,
    title = title,
    content = content,
    authorId = authorId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    mediaUrls = mediaUrls,
    isEdited = isEdited
)
