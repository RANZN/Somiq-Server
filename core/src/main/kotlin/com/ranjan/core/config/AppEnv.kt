package com.ranjan.core.config

enum class AppEnv {
    LOCAL,
    PRODUCTION;

    val isProduction: Boolean get() = this == PRODUCTION
    val isLocal: Boolean get() = this == LOCAL
}
