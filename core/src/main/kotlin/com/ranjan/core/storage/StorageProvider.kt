package com.ranjan.core.storage

import com.ranjan.core.config.AppEnv

enum class StorageProvider {
    LOCAL,
    GCS;

    val isGcs: Boolean get() = this == GCS
    val isLocal: Boolean get() = this == LOCAL

    companion object {
        fun fromString(value: String?, env: AppEnv = AppEnv.LOCAL): StorageProvider {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: if (env.isProduction) GCS else LOCAL
        }
    }
}
