package com.ranjan.core.storage.gcs

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.ranjan.core.config.StorageConfig
import java.io.FileInputStream

object GcsClientFactory {

    fun create(config: StorageConfig.Gcs): Storage {
        val optionsBuilder = StorageOptions.newBuilder()

        if (!config.projectId.isNullOrBlank()) {
            optionsBuilder.setProjectId(config.projectId)
        }

        val credentials = when {
            !config.credentialsJson.isNullOrBlank() ->
                GoogleCredentials.fromStream(config.credentialsJson.byteInputStream())

            !config.credentialsPath.isNullOrBlank() ->
                FileInputStream(config.credentialsPath).use { stream ->
                    GoogleCredentials.fromStream(stream)
                }

            else -> runCatching { GoogleCredentials.getApplicationDefault() }.getOrNull()
        }

        credentials?.let { optionsBuilder.setCredentials(it) }
        return optionsBuilder.build().service
    }
}