package com.ranjan.domain.collection.model

import kotlinx.serialization.Serializable

@Serializable
data class AddItemRequest(
    val itemType: ItemType,
    val itemId: String // postId or reelId
)

