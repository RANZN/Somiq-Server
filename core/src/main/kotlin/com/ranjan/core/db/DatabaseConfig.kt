package com.ranjan.core.db

import com.ranjan.core.config.Env

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 10
) {
    companion object {
        fun from(env: Env): DatabaseConfig {
            val driver = env.require("DB_DRIVER")
            val url = env.require("DB_URL")
            val user = env.require("DB_USER")
            val password = env.optional("DB_PASSWORD") ?: ""
            val maxPoolSize = env.int("DB_MAX_POOL_SIZE", 10)
            env.validate()
            return DatabaseConfig(
                driver = driver,
                url = url,
                user = user,
                password = password,
                maxPoolSize = maxPoolSize
            )
        }
    }
}
