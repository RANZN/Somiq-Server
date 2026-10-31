package com.ranjan.core.config

import com.ranjan.core.config.serializer.CommaSeparatedListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** CORS settings, read from the `cors` section of application.yaml. [env] is filled in from `app.env`. */
@Serializable
data class CorsConfig(
    @Transient
    val env: AppEnv = AppEnv.LOCAL,
    @Serializable(with = CommaSeparatedListSerializer::class)
    val allowedHosts: List<String> = emptyList()
)
