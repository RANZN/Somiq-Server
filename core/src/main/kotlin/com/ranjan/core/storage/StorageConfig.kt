package com.ranjan.core.storage

import com.ranjan.core.config.AppConfig
import com.ranjan.core.config.AppEnv

data class StorageConfig(
    val provider: StorageProvider = StorageProvider.LOCAL,
    val gcsBucket: String = "somiq-uploads",
    val gcsProjectId: String? = null,
    val gcsCredentialsPath: String? = null,
    val gcsCredentialsJson: String? = null,
    val gcsPublicUrlPrefix: String = "https://storage.googleapis.com/$gcsBucket"
) {
    val isGcs: Boolean get() = provider == StorageProvider.GCS
    val isLocal: Boolean get() = provider == StorageProvider.LOCAL

    companion object {
        fun fromEnv(env: AppEnv = AppEnv.LOCAL): StorageConfig =
            AppConfig.determineStorageConfig(env)
    }
}
