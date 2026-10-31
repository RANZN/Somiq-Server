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
            "app.corsHosts" to "localhost, 127.0.0.1",
            "jwt.secret" to "test-secret-key-that-is-at-least-32-characters",
            "jwt.issuer" to "somiq-server",
            "jwt.audience" to "somiq-app",
            "jwt.signupAudience" to "somiq-signup",
            "jwt.realm" to "auth-jwt",
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

    @Test
    fun `test AppEnv enum values and flags`() {
        assertEquals(listOf(AppEnv.LOCAL, AppEnv.PRODUCTION), AppEnv.entries)
        assertTrue(AppEnv.PRODUCTION.isProduction)
        assertFalse(AppEnv.LOCAL.isProduction)
    }

    @Test
    fun `test CorsConfig initialization`() {
        val prodCors = CorsConfig(
            env = AppEnv.PRODUCTION,
            allowedHosts = listOf("example.com", "api.example.com")
        )
        assertEquals(AppEnv.PRODUCTION, prodCors.env)
        assertEquals(2, prodCors.allowedHosts.size)

        val localCors = CorsConfig(env = AppEnv.LOCAL)
        assertEquals(emptyList(), localCors.allowedHosts)
    }

    @Test
    fun `test CorsConfig from splits comma separated hosts`() {
        val cors = CorsConfig.from(testConfig(), AppEnv.LOCAL)
        assertEquals(listOf("localhost", "127.0.0.1"), cors.allowedHosts)
    }

    @Test
    fun `test StorageProvider enum values`() {
        assertEquals(listOf(StorageProvider.LOCAL, StorageProvider.GCS, StorageProvider.STRATUS), StorageProvider.entries)
    }

    @Test
    fun `test StorageConfig from returns Local when selected`() {
        assertEquals(StorageConfig.Local, StorageConfig.from(testConfig()))
    }

    @Test
    fun `test StorageConfig defaults to Stratus and requires its settings`() {
        val config = MapApplicationConfig("app.env" to "LOCAL")
        val ex = assertFailsWith<IllegalStateException> { StorageConfig.from(config) }
        assertTrue(ex.message!!.contains("storage.stratus.bucketUrl"))
    }

    @Test
    fun `test StorageConfig from returns Stratus with OAuth settings`() {
        val storage = assertIs<StorageConfig.Stratus>(
            StorageConfig.from(
                testConfig(
                    "storage.provider" to "STRATUS",
                    "storage.stratus.bucketUrl" to "https://somiq-uploads-development.zohostratus.in",
                    "storage.stratus.clientId" to "client-id",
                    "storage.stratus.clientSecret" to "client-secret",
                    "storage.stratus.refreshToken" to "refresh-token"
                )
            )
        )
        assertEquals("https://somiq-uploads-development.zohostratus.in", storage.bucketUrl)
        assertEquals("https://accounts.zoho.in", storage.oauth.accountsUrl)
    }

    @Test
    fun `test StorageConfig from returns Gcs with default public URL`() {
        val storage = assertIs<StorageConfig.Gcs>(
            StorageConfig.from(
                testConfig("storage.provider" to "GCS", "storage.gcs.bucket" to "somiq-uploads")
            )
        )
        assertEquals("https://storage.googleapis.com/somiq-uploads", storage.publicUrlPrefix)
    }

    @Test
    fun `test AppConfig from aggregates all subconfigs`() {
        val config = testConfig()
        val appConfig = AppConfig.from(config)
        assertEquals(AppEnv.LOCAL, appConfig.env)
        assertEquals(StorageConfig.from(config), appConfig.storage)
        assertEquals("org.h2.Driver", appConfig.database.driver)
        assertEquals("somiq-server", appConfig.jwt.issuer)
    }

    @Test
    fun `test AppConfig from rejects invalid app env`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(testConfig("app.env" to "STAGING"))
        }
        assertTrue(ex.message!!.contains("app.env"))
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

    @Test
    fun `test AppConfig from throws when a required value is missing`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(testConfig("jwt.secret" to ""))
        }
        assertTrue(ex.message!!.contains("jwt.secret"))
    }
}