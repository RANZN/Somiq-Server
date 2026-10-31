package com.ranjan.core.config

import io.ktor.server.config.ApplicationConfig

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 10
) {
    companion object {
        fun from(config: ApplicationConfig): DatabaseConfig {
            val url = config.requireString("database.url")
            return DatabaseConfig(
                driver = driverFor(url),
                url = url,
                user = config.requireString("database.user"),
                password = config.optionalString("database.password") ?: "",
                maxPoolSize = config.optionalInt("database.maxPoolSize", 10)
            )
        }

        fun driverFor(url: String): String = when {
            url.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
            url.startsWith("jdbc:h2:") -> "org.h2.Driver"
            else -> error("Unsupported database url '$url', expected jdbc:postgresql: or jdbc:h2:")
        }
    }
}