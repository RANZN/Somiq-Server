package com.ranjan.core.config

import com.ranjan.core.config.serializer.CommaSeparatedListSerializer
import kotlinx.serialization.Serializable

/** CORS settings, read from the `cors` section of application.yaml. */
@Serializable
data class CorsConfig(
    @Serializable(with = CommaSeparatedListSerializer::class)
    val allowedHosts: List<String> = emptyList()
)
