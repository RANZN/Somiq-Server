package com.ranjan.core.config

import io.ktor.server.config.ApplicationConfig
import kotlin.time.Duration

internal fun ApplicationConfig.optionalString(path: String): String? =
    propertyOrNull(path)?.getString()?.trim()?.takeIf { it.isNotEmpty() }

internal fun ApplicationConfig.requireString(path: String): String =
    optionalString(path) ?: error("Missing required configuration '$path'")

internal fun ApplicationConfig.requireDuration(path: String): Duration {
    val value = requireString(path)
    return Duration.parseOrNull(value)
        ?: Duration.parseOrNull(value.replace(" ", ""))
        ?: error("Invalid duration '$value' for '$path', expected format like '1h', '7d', '30m'")
}

internal fun ApplicationConfig.optionalInt(path: String, default: Int): Int {
    val value = optionalString(path) ?: return default
    return value.toIntOrNull() ?: error("Invalid integer '$value' for '$path'")
}

internal inline fun <reified T : Enum<T>> ApplicationConfig.enum(path: String, default: T): T {
    val value = optionalString(path) ?: return default
    return enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
        ?: error("Invalid value '$value' for '$path', must be one of: ${enumValues<T>().joinToString { it.name }}")
}