package com.ranjan.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String
)