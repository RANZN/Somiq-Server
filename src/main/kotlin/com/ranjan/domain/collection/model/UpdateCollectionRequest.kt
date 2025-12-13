package com.ranjan.domain.collection.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null
)

