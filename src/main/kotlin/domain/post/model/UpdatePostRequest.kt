package com.ranjan.domain.post.model


data class UpdatePostRequest(
    val title: String?,
    val content: String?,
    val mediaUrls: List<String>? = null
)
