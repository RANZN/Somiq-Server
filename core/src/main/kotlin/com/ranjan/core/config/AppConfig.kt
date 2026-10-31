package com.ranjan.core.config

import io.ktor.server.config.ApplicationConfig

data class AppConfig(
    val env: AppEnv,
    val cors: CorsConfig,
    val storage: StorageConfig,
    val database: DatabaseConfig,
    val jwt: JwtConfig
) {
    companion object {
        fun from(config: ApplicationConfig): AppConfig {
            val appEnv = config.enum("app.env", AppEnv.LOCAL)
            return AppConfig(
                env = appEnv,
                cors = CorsConfig.from(config, appEnv),
                storage = StorageConfig.from(config),
                database = DatabaseConfig.from(config),
                jwt = JwtConfig.from(config)
            )
        }
    }
}