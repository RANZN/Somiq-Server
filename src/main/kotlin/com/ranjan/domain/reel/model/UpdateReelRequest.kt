package com.ranjan.domain.reel.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReelRequest(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null
)

