package com.ranjan.domain.post.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePostRequest(
    val caption: String?,
    val mediaUrls: List<String>? = null
)
