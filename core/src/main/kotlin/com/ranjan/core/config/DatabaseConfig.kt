package com.ranjan.core.config

import kotlinx.serialization.Serializable

/** Database settings, read from the `database` section of application.yaml. */
@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String = "",
    val maxPoolSize: Int = 10
) {
    init {
        require(url.isNotBlank()) { "Database url must not be blank (set DB_URL)" }
        require(user.isNotBlank()) { "Database user must not be blank (set DB_USER)" }
        require(maxPoolSize > 0) { "Database maxPoolSize must be positive" }
        driverFor(url)
    }

    /** JDBC driver class, derived from [url]. */
    val driver: String get() = driverFor(url)

    companion object {
        fun driverFor(url: String): String = when {
            url.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
            url.startsWith("jdbc:h2:") -> "org.h2.Driver"
            else -> throw IllegalArgumentException(
                "Unsupported database url '$url', expected jdbc:postgresql: or jdbc:h2:"
            )
        }
    }
}
