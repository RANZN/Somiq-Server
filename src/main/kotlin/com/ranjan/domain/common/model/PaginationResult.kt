package com.ranjan.domain.common.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginationResult<T>(
    val data: List<T>,
    val nextCursor: String?       // null means end of list
)