package com.ranjan.domain.collection.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionResponse(
    val collectionId: String,
    val name: String,
    val description: String?,
    val itemsCount: Long,
    val createdAt: Long,
    val updatedAt: Long?
)

