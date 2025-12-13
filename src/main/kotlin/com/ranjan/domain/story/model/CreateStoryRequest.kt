package com.ranjan.domain.story.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateStoryRequest(
    val mediaUrl: String,
    val mediaType: MediaType
)

