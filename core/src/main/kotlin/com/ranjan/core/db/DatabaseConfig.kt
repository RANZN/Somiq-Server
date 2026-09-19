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
        fun get(): DatabaseConfig = DatabaseConfig(
            driver = Env.dbDriver,
            url = Env.dbUrl,
            user = Env.dbUser,
            password = Env.dbPassword,
            maxPoolSize = Env.dbMaxPoolSize
        )
    }
}
