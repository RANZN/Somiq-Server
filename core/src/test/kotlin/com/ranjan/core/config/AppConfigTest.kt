package com.ranjan.core.config

import com.ranjan.core.di.configModule
import com.ranjan.core.di.coreModule
import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.StorageProvider
import io.ktor.server.config.MapApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppConfigTest {

    private fun testConfig(vararg overrides: Pair<String, String>): MapApplicationConfig {
        val values = linkedMapOf(
            "app.env" to "LOCAL",
            "cors.allowedHosts" to "localhost, 127.0.0.1",
            "jwt.secret" to "test-secret-key-that-is-at-least-32-characters",
            "jwt.issuer" to "somiq-server",
            "jwt.audience" to "somiq-app",
            "jwt.signupAudience" to "somiq-signup",
            "jwt.accessTokenLifetime" to "1h",
            "jwt.refreshTokenLifetime" to "7d",
            "jwt.signupTokenLifetime" to "30m",
            "database.url" to "jdbc:h2:mem:appconfig;DB_CLOSE_DELAY=-1",
            "database.user" to "sa",
            "storage.provider" to "LOCAL"
        )
        values.putAll(overrides)
        return MapApplicationConfig(*values.toList().toTypedArray())
    }

    private val stratusValues = arrayOf(
        "storage.provider" to "STRATUS",
        "storage.stratus.bucketUrl" to "https://somiq-uploads-development.zohostratus.in",
        "storage.stratus.oauth.clientId" to "client-id",
        "storage.stratus.oauth.clientSecret" to "client-secret",
        "storage.stratus.oauth.refreshToken" to "refresh-token"
    )

    @Test
    fun `test AppEnv enum values and flags`() {
        assertEquals(listOf(AppEnv.LOCAL, AppEnv.PRODUCTION), AppEnv.entries)
        assertTrue(AppEnv.PRODUCTION.isProduction)
        assertFalse(AppEnv.LOCAL.isProduction)
    }

    @Test
    fun `test StorageProvider enum values`() {
        assertEquals(listOf(StorageProvider.LOCAL, StorageProvider.GCS, StorageProvider.STRATUS), StorageProvider.entries)
    }

    @Test
    fun `test AppConfig from aggregates all sections`() {
        val appConfig = AppConfig.from(testConfig())
        assertEquals(AppEnv.LOCAL, appConfig.env)
        assertEquals(StorageConfig.Local, appConfig.storage)
        assertEquals("org.h2.Driver", appConfig.database.driver)
        assertEquals("somiq-server", appConfig.jwt.issuer)
    }

    @Test
    fun `test AppConfig from reads app env case-insensitively`() {
        assertEquals(AppEnv.PRODUCTION, AppConfig.from(testConfig("app.env" to "production")).env)
    }

    @Test
    fun `test AppConfig from rejects invalid app env`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            AppConfig.from(testConfig("app.env" to "STAGING"))
        }
        assertTrue(ex.message!!.contains("app.env"))
    }

    @Test
    fun `test CorsConfig splits comma separated hosts and takes env from app section`() {
        val cors = AppConfig.from(testConfig("app.env" to "PRODUCTION")).cors
        assertEquals(AppEnv.PRODUCTION, cors.env)
        assertEquals(listOf("localhost", "127.0.0.1"), cors.allowedHosts)
    }

    @Test
    fun `test CorsConfig defaults`() {
        val cors = CorsConfig()
        assertEquals(AppEnv.LOCAL, cors.env)
        assertEquals(emptyList(), cors.allowedHosts)
    }

    @Test
    fun `test storage defaults to Stratus when provider is not set`() {
        val config = testConfig(*stratusValues)
        config.put("storage.provider", "")
        assertIs<StorageConfig.Stratus>(AppConfig.from(config).storage)
    }

    @Test
    fun `test storage reads Stratus with OAuth settings`() {
        val storage = assertIs<StorageConfig.Stratus>(AppConfig.from(testConfig(*stratusValues)).storage)
        assertEquals("https://somiq-uploads-development.zohostratus.in", storage.bucketUrl)
        assertEquals("client-id", storage.oauth.clientId)
        assertEquals("https://accounts.zoho.in", storage.oauth.accountsUrl)
    }

    @Test
    fun `test storage rejects blank Stratus settings`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            AppConfig.from(testConfig(*stratusValues, "storage.stratus.bucketUrl" to ""))
        }
        assertTrue(ex.message!!.contains("STRATUS_BUCKET_URL"))
    }

    @Test
    fun `test storage reads Gcs with default public URL`() {
        val storage = assertIs<StorageConfig.Gcs>(
            AppConfig.from(
                testConfig("storage.provider" to "GCS", "storage.gcs.bucket" to "somiq-uploads")
            ).storage
        )
        assertEquals("https://storage.googleapis.com/somiq-uploads", storage.resolvedPublicUrlPrefix)
    }

    @Test
    fun `test AppConfig from rejects blank JWT secret`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            AppConfig.from(testConfig("jwt.secret" to ""))
        }
        assertTrue(ex.message!!.contains("JWT_SECRET"))
    }

    @Test
    fun `test koin resolution of AppConfig, StorageConfig, and MediaStorageService`() {
        val koinApp = koinApplication {
            modules(configModule(AppConfig.from(testConfig())), coreModule)
        }
        val appConfig = koinApp.koin.get<AppConfig>()

        assertEquals(appConfig.storage, koinApp.koin.get<StorageConfig>())
        assertEquals(appConfig.jwt, koinApp.koin.get<JwtConfig>())
        assertNotNull(koinApp.koin.get<MediaStorageService>())
        assertNotNull(koinApp.koin.get<Database>())
    }
}
