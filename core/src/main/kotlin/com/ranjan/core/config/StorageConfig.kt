package com.ranjan.core.config

import com.ranjan.core.storage.StorageProvider
import io.ktor.server.config.ApplicationConfig

sealed interface StorageConfig {

    data object Local : StorageConfig

    data class Gcs(
        val bucket: String,
        val publicUrlPrefix: String,
        val projectId: String? = null,
        val credentialsPath: String? = null,
        val credentialsJson: String? = null
    ) : StorageConfig

    data class Stratus(
        val bucketUrl: String,
        val oauth: ZohoOAuth
    ) : StorageConfig

    data class ZohoOAuth(
        val accountsUrl: String,
        val clientId: String,
        val clientSecret: String,
        val refreshToken: String
    )

    companion object {
        private const val DEFAULT_ZOHO_ACCOUNTS_URL = "https://accounts.zoho.in"

        fun from(config: ApplicationConfig): StorageConfig =
            when (config.enum("storage.provider", StorageProvider.STRATUS)) {
                StorageProvider.STRATUS -> stratus(config)
                StorageProvider.GCS -> gcs(config)
                StorageProvider.LOCAL -> Local
            }

        private fun gcs(config: ApplicationConfig): Gcs {
            val bucket = config.requireString("storage.gcs.bucket")
            return Gcs(
                bucket = bucket,
                publicUrlPrefix = config.optionalString("storage.gcs.publicUrlPrefix")
                    ?: "https://storage.googleapis.com/$bucket",
                projectId = config.optionalString("storage.gcs.projectId"),
                credentialsPath = config.optionalString("storage.gcs.credentialsPath"),
                credentialsJson = config.optionalString("storage.gcs.credentialsJson")
            )
        }

        private fun stratus(config: ApplicationConfig): Stratus = Stratus(
            bucketUrl = config.requireString("storage.stratus.bucketUrl"),
            oauth = ZohoOAuth(
                accountsUrl = config.optionalString("storage.stratus.accountsUrl") ?: DEFAULT_ZOHO_ACCOUNTS_URL,
                clientId = config.requireString("storage.stratus.clientId"),
                clientSecret = config.requireString("storage.stratus.clientSecret"),
                refreshToken = config.requireString("storage.stratus.refreshToken")
            )
        )
    }
}