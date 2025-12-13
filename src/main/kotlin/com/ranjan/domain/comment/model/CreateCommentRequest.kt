package com.ranjan.domain.comment.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequest(
    val content: String,
    val parentCommentId: String? = null // For nested replies
)

