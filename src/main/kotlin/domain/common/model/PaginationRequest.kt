package com.ranjan.domain.common.model

data class PaginationRequest(
    val limit: Int = 20,
    val cursor: String? = null   // id or timestamp of last item
)