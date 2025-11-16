package com.ranjan.domain.post.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePostRequest(
    val title: String?,
    val content: String?,
    val mediaUrls: List<String>? = null
)
