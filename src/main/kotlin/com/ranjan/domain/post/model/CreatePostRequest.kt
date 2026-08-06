package com.ranjan.domain.post.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val caption: String,
    val mediaUrls: List<ByteArray> = emptyList()
)