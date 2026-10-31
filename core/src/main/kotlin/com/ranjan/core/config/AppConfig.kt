package com.ranjan.core.config

import com.ranjan.core.storage.StorageProvider
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs

data class AppConfig(
    val env: AppEnv,
    val cors: CorsConfig,
    val storage: StorageConfig,
    val database: DatabaseConfig,
    val jwt: JwtConfig
) {
    companion object {
        /**
         * Builds the app configuration from application.yaml.
         * This is the only place that reads Ktor's [ApplicationConfig]; each section is
         * decoded straight into its typed config class.
         */
        fun from(config: ApplicationConfig): AppConfig {
            val env = config.enum("app.env", AppEnv.LOCAL)
            return AppConfig(
                env = env,
                cors = config.property("cors").getAs<CorsConfig>().copy(env = env),
                storage = storage(config),
                database = config.property("database").getAs(),
                jwt = config.property("jwt").getAs()
            )
        }

        private fun storage(config: ApplicationConfig): StorageConfig =
            when (config.enum("storage.provider", StorageProvider.STRATUS)) {
                StorageProvider.STRATUS -> config.property("storage.stratus").getAs<StorageConfig.Stratus>()
                StorageProvider.GCS -> config.property("storage.gcs").getAs<StorageConfig.Gcs>()
                StorageProvider.LOCAL -> StorageConfig.Local
            }

        private inline fun <reified T : Enum<T>> ApplicationConfig.enum(path: String, default: T): T {
            val value = propertyOrNull(path)?.getString()?.trim()?.takeIf { it.isNotEmpty() } ?: return default
            return enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Invalid value '$value' for '$path', must be one of: ${enumValues<T>().joinToString { it.name }}"
                )
        }
    }
}
