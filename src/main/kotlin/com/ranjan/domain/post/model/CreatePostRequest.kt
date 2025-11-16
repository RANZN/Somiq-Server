package com.ranjan.domain.post.model

import kotlinx.serialization.Serializable


@Serializable
data class CreatePostRequest(
    val title: String,
    val content: String,
    val mediaUrls: List<String> = emptyList()
)
