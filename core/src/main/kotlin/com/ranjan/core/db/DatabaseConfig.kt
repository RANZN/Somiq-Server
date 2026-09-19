package com.ranjan.core.db

import com.ranjan.core.config.AppEnv

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 10
) {
    companion object {
        fun fromEnv(env: AppEnv = AppEnv.LOCAL): DatabaseConfig {
            val isProd = env.isProduction
            val defaultDriver = if (isProd) "org.postgresql.Driver" else "org.h2.Driver"
            val defaultUrl = if (isProd) "" else "jdbc:h2:file:./build/db"
            val defaultUser = if (isProd) "" else "root"
            val defaultPoolSize = if (isProd) 10 else 5

            return DatabaseConfig(
                driver = System.getenv("DB_DRIVER") ?: defaultDriver,
                url = System.getenv("DB_URL") ?: defaultUrl,
                user = System.getenv("DB_USER") ?: defaultUser,
                password = System.getenv("DB_PASSWORD") ?: "",
                maxPoolSize = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: defaultPoolSize
            )
        }
    }
}
