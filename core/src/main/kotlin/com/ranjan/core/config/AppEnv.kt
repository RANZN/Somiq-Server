package com.ranjan.core.config

enum class AppEnv {
    LOCAL,
    STAGING,
    PRODUCTION;

    val isProduction: Boolean get() = this == PRODUCTION

    companion object {
        fun fromString(value: String?): AppEnv {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: STAGING // safe default fallback
        }
    }
}
