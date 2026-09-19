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
        fun get() = StorageConfig(
            provider = Env.storageProvider,
            gcsBucket = Env.gcsBucketName,
            gcsProjectId = Env.gcsProjectId,
            gcsCredentialsPath = Env.gcsCredentialsPath,
            gcsCredentialsJson = Env.gcsCredentialsJson,
            gcsPublicUrlPrefix = Env.gcsPublicUrlPrefix
        )
    }
}
