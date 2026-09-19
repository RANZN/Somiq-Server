package com.ranjan.core.config

import com.ranjan.core.db.DatabaseConfig
import com.ranjan.core.storage.StorageConfig
import com.ranjan.core.storage.StorageProvider

data class AppConfig(
    val env: AppEnv,
    val cors: CorsConfig,
    val storage: StorageConfig,
    val database: DatabaseConfig,
    val jwt: JwtConfig,
    val bucketName: String = storage.gcsBucket,
    val dbUrl: String = database.url
) {
    companion object {
        fun load(): AppConfig {
            val env = AppEnv.fromString(System.getenv("APP_ENV"))
            val cors = CorsConfig.fromEnv(env)
            val storage = determineStorageConfig(env)
            val database = determineDatabaseConfig(env)
            val jwt = determineJwtConfig(env)

            return AppConfig(
                env = env,
                cors = cors,
                storage = storage,
                database = database,
                jwt = jwt,
                bucketName = storage.gcsBucket,
                dbUrl = database.url
            )
        }

        fun determineStorageConfig(env: AppEnv): StorageConfig {
            val provider = StorageProvider.fromString(
                value = System.getenv("STORAGE_PROVIDER"),
                env = env
            )
            val bucket = System.getenv("GCS_BUCKET_NAME")
                ?: System.getenv("BUCKET_NAME")
                ?: "somiq-uploads"

            return StorageConfig(
                provider = provider,
                gcsBucket = bucket,
                gcsProjectId = System.getenv("GCS_PROJECT_ID"),
                gcsCredentialsPath = System.getenv("GCS_CREDENTIALS_PATH")
                    ?: System.getenv("GOOGLE_APPLICATION_CREDENTIALS"),
                gcsCredentialsJson = System.getenv("GCS_CREDENTIALS_JSON"),
                gcsPublicUrlPrefix = System.getenv("GCS_PUBLIC_URL_PREFIX")
                    ?: "https://storage.googleapis.com/$bucket"
            )
        }

        fun determineDatabaseConfig(env: AppEnv): DatabaseConfig =
            DatabaseConfig.fromEnv(env)

        fun determineJwtConfig(env: AppEnv): JwtConfig =
            JwtConfig.fromEnv(env)
    }
}
