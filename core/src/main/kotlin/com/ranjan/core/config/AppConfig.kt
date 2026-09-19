package com.ranjan.core.config

import com.ranjan.core.db.DatabaseConfig
import com.ranjan.core.storage.StorageConfig

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
            val env = Env.appEnv
            val cors = CorsConfig.fromEnv(env)
            val storage = StorageConfig.get()
            val database = DatabaseConfig.get()

            return AppConfig(
                env = env,
                cors = cors,
                storage = storage,
                database = database,
                jwt = JwtConfig.get(),
                bucketName = storage.gcsBucket,
                dbUrl = database.url
            )
        }

        fun determineStorageConfig(): StorageConfig = StorageConfig.get()

        fun determineStorageConfig(env: AppEnv): StorageConfig = StorageConfig.get()
    }
}