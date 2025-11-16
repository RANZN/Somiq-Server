package com.ranjan.domain.common.model

data class PaginationResult<T>(
    val items: List<T>,
    val nextCursor: String?       // null means end of list
)