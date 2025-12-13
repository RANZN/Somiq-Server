package com.ranjan.domain.comment.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCommentRequest(
    val content: String
)

