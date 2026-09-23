package com.ranjan.core.storage.gcs

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.ranjan.core.storage.StorageConfig
import java.io.FileInputStream

object GcsClientFactory {

    fun create(config: StorageConfig): Storage {
        val optionsBuilder = StorageOptions.newBuilder()

        if (!config.gcsProjectId.isNullOrBlank()) {
            optionsBuilder.setProjectId(config.gcsProjectId)
        }

        val credentials = when {
            !config.gcsCredentialsJson.isNullOrBlank() ->
                GoogleCredentials.fromStream(config.gcsCredentialsJson.byteInputStream())

            !config.gcsCredentialsPath.isNullOrBlank() ->
                FileInputStream(config.gcsCredentialsPath).use { stream ->
                    GoogleCredentials.fromStream(stream)
                }

            else -> runCatching { GoogleCredentials.getApplicationDefault() }.getOrNull()
        }

        credentials?.let { optionsBuilder.setCredentials(it) }
        return optionsBuilder.build().service
    }
}
