package com.ranjan.core.config

import kotlinx.serialization.Serializable

/** Media storage settings, read from the `storage` section of application.yaml. */
sealed interface StorageConfig {

    data object Local : StorageConfig

    @Serializable
    data class Gcs(
        val bucket: String,
        val publicUrlPrefix: String = "",
        val projectId: String? = null,
        val credentialsPath: String? = null,
        val credentialsJson: String? = null
    ) : StorageConfig {
        init {
            require(bucket.isNotBlank()) { "GCS bucket must not be blank (set GCS_BUCKET_NAME)" }
        }

        /** Public URL prefix for stored files, defaulting to the bucket's googleapis.com URL. */
        val resolvedPublicUrlPrefix: String
            get() = publicUrlPrefix.ifBlank { "https://storage.googleapis.com/$bucket" }
    }

    @Serializable
    data class Stratus(
        val bucketUrl: String,
        val oauth: ZohoOAuth
    ) : StorageConfig {
        init {
            require(bucketUrl.isNotBlank()) { "Stratus bucketUrl must not be blank (set STRATUS_BUCKET_URL)" }
        }
    }

    @Serializable
    data class ZohoOAuth(
        val accountsUrl: String = "https://accounts.zoho.in",
        val clientId: String,
        val clientSecret: String,
        val refreshToken: String
    ) {
        init {
            require(accountsUrl.isNotBlank()) { "Zoho accountsUrl must not be blank" }
            require(clientId.isNotBlank()) { "Zoho clientId must not be blank (set ZOHO_CLIENT_ID)" }
            require(clientSecret.isNotBlank()) { "Zoho clientSecret must not be blank (set ZOHO_CLIENT_SECRET)" }
            require(refreshToken.isNotBlank()) { "Zoho refreshToken must not be blank (set ZOHO_REFRESH_TOKEN)" }
        }
    }
}
