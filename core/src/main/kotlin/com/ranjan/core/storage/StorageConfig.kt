package com.ranjan.core.storage

import com.ranjan.core.config.Env

data class StorageConfig(
    val provider: StorageProvider = StorageProvider.LOCAL,
    val gcsBucket: String = "somiq-uploads",
    val gcsProjectId: String? = null,
    val gcsCredentialsPath: String? = null,
    val gcsCredentialsJson: String? = null,
    val gcsPublicUrlPrefix: String = "https://storage.googleapis.com/$gcsBucket"
) {
    companion object {
        fun from(env: Env): StorageConfig {
            val provider = env.enum("STORAGE_PROVIDER", StorageProvider.LOCAL)
            val gcsBucket = env.require("GCS_BUCKET_NAME")
            val gcsProjectId = env.optional("GCS_PROJECT_ID")
            val gcsCredentialsPath = env.optional("GCS_CREDENTIALS_PATH") ?: env.optional("GOOGLE_APPLICATION_CREDENTIALS")
            val gcsCredentialsJson = env.optional("GCS_CREDENTIALS_JSON")
            val gcsPublicUrlPrefix = env.require("GCS_PUBLIC_URL_PREFIX")
            env.validate()
            return StorageConfig(
                provider = provider,
                gcsBucket = gcsBucket,
                gcsProjectId = gcsProjectId,
                gcsCredentialsPath = gcsCredentialsPath,
                gcsCredentialsJson = gcsCredentialsJson,
                gcsPublicUrlPrefix = gcsPublicUrlPrefix
            )
        }
    }
}
