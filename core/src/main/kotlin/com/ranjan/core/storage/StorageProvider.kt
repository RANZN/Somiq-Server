package com.ranjan.core.storage

enum class StorageProvider {
    LOCAL,
    GCS;

    val isGcs: Boolean get() = this == GCS
    val isLocal: Boolean get() = this == LOCAL

    companion object {
        fun fromString(value: String?): StorageProvider {
            val trimmed = value?.trim()
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Missing or invalid STORAGE_PROVIDER: '$value'. Must be one of: ${entries.joinToString()}"
                )
        }
    }
}
