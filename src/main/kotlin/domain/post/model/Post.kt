package com.ranjan.domain.post.model


data class Post(
    val id: String,
    val authorId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val mediaUrls: List<String> = emptyList(),
    val isEdited: Boolean = false
)
