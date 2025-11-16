package com.ranjan.domain.post.model


data class CreatePostRequest(
    val title: String,
    val content: String,
    val mediaUrls: List<String> = emptyList()
)
