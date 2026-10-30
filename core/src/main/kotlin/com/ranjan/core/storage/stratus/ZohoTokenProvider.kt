package com.ranjan.core.storage.stratus

import com.ranjan.core.config.StorageConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ZohoTokenProvider(
    private val httpClient: HttpClient,
    private val oauth: StorageConfig.ZohoOAuth
) {
    private val mutex = Mutex()
    private var accessToken: String? = null
    private var expiresAtMs: Long = 0

    suspend fun token(): String {
        mutex.withLock {
            val now = System.currentTimeMillis()
            accessToken?.let { if (now < expiresAtMs) return it }

            val body = httpClient.submitForm(
                url = "${oauth.accountsUrl.trimEnd('/')}/oauth/v2/token",
                formParameters = parameters {
                    append("refresh_token", oauth.refreshToken)
                    append("client_id", oauth.clientId)
                    append("client_secret", oauth.clientSecret)
                    append("grant_type", "refresh_token")
                }
            ).bodyAsText()

            val response = json.decodeFromString<TokenResponse>(body)
            val token = response.accessToken
                ?: error("Zoho token refresh failed: ${response.error ?: body}")

            accessToken = token
            expiresAtMs = now + (response.expiresIn - 60) * 1000
            return token
        }
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token") val accessToken: String? = null,
        @SerialName("expires_in") val expiresIn: Long = 3600,
        val error: String? = null
    )

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}