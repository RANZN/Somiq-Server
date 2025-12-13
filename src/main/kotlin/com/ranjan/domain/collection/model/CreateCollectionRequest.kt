package com.ranjan.domain.collection.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateCollectionRequest(
    val name: String,
    val description: String? = null
)

