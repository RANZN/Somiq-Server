package com.ranjan.domain.collection.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionItemResponse(
    val itemId: String,
    val collectionId: String,
    val itemType: ItemType,
    val itemRefId: String, // postId or reelId
    val addedAt: Long
)

@Serializable
enum class ItemType {
    POST,
    REEL
}

