package com.ranjan.domain.reel.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateReelRequest(
    val title: String,
    val description: String? = null,
    val videoUrl: String,
    val thumbnailUrl: String? = null
)

