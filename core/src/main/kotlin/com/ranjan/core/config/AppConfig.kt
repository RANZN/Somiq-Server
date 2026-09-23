package com.ranjan.core.config

import com.ranjan.core.db.DatabaseConfig

data class AppConfig(
    val env: AppEnv,
    val cors: CorsConfig,
    val storage: StorageConfig,
    val database: DatabaseConfig,
    val jwt: JwtConfig
) {
    companion object {
        fun from(env: Env): AppConfig {
            val appEnv = env.enum("APP_ENV", AppEnv.LOCAL)
            val cors = runCatching { CorsConfig.from(env) }.getOrNull()
            val storage = runCatching { StorageConfig.from(env) }.getOrNull()
            val database = runCatching { DatabaseConfig.from(env) }.getOrNull()
            val jwt = runCatching { JwtConfig.from(env) }.getOrNull()

            env.validate()

            return AppConfig(
                env = appEnv,
                cors = cors!!,
                storage = storage!!,
                database = database!!,
                jwt = jwt!!
            )
        }
    }
}