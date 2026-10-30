package com.ranjan.core.config

import com.ranjan.core.storage.StorageProvider

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
        fun from(env: Env): StorageConfig {
            val config = when (env.enum("STORAGE_PROVIDER", StorageProvider.LOCAL)) {
                StorageProvider.STRATUS -> stratus(env)
                StorageProvider.GCS -> gcs(env)
                StorageProvider.LOCAL -> Local
            }
            env.validate()
            return config
        }

        private fun gcs(env: Env): Gcs {
            val bucket = env.require("GCS_BUCKET_NAME")
            return Gcs(
                bucket = bucket,
                publicUrlPrefix = env.optional("GCS_PUBLIC_URL_PREFIX") ?: "https://storage.googleapis.com/$bucket",
                projectId = env.optional("GCS_PROJECT_ID"),
                credentialsPath = env.optional("GCS_CREDENTIALS_PATH") ?: env.optional("GOOGLE_APPLICATION_CREDENTIALS"),
                credentialsJson = env.optional("GCS_CREDENTIALS_JSON")
            )
        }

        private fun stratus(env: Env): Stratus = Stratus(
            bucketUrl = env.require("STRATUS_BUCKET_URL"),
            oauth = ZohoOAuth(
                accountsUrl = env.optional("ZOHO_ACCOUNTS_URL") ?: "https://accounts.zoho.in",
                clientId = env.require("ZOHO_CLIENT_ID"),
                clientSecret = env.require("ZOHO_CLIENT_SECRET"),
                refreshToken = env.require("ZOHO_REFRESH_TOKEN")
            )
        )
    }
}