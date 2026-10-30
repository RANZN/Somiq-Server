package com.ranjan.core.config

import com.ranjan.core.db.DatabaseConfig
import com.ranjan.core.storage.StorageProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppConfigTest {

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
    fun `test StorageProvider enum values`() {
        assertEquals(listOf(StorageProvider.LOCAL, StorageProvider.GCS, StorageProvider.STRATUS), StorageProvider.entries)
    }

    @Test
    fun `test StorageConfig from returns Local by default`() {
        assertEquals(StorageConfig.Local, StorageConfig.from(Env.from(emptyMap())))
    }

    @Test
    fun `test StorageConfig from returns Stratus with OAuth settings`() {
        val storage = kotlin.test.assertIs<StorageConfig.Stratus>(
            StorageConfig.from(
                Env.from(
                    mapOf(
                        "STORAGE_PROVIDER" to "STRATUS",
                        "STRATUS_BUCKET_URL" to "https://somiq-uploads-development.zohostratus.in",
                        "ZOHO_CLIENT_ID" to "client-id",
                        "ZOHO_CLIENT_SECRET" to "client-secret",
                        "ZOHO_REFRESH_TOKEN" to "refresh-token"
                    )
                )
            )
        )
        assertEquals("https://somiq-uploads-development.zohostratus.in", storage.bucketUrl)
        assertEquals("https://accounts.zoho.in", storage.oauth.accountsUrl)
    }

    @Test
    fun `test StorageConfig from requires Stratus credentials only when Stratus is selected`() {
        val ex = kotlin.test.assertFailsWith<IllegalStateException> {
            StorageConfig.from(Env.from(mapOf("STORAGE_PROVIDER" to "STRATUS")))
        }
        assertTrue(ex.message!!.contains("STRATUS_BUCKET_URL"))
        assertTrue(ex.message!!.contains("ZOHO_REFRESH_TOKEN"))
    }

    @Test
    fun `test StorageConfig from returns Gcs with default public URL`() {
        val storage = kotlin.test.assertIs<StorageConfig.Gcs>(
            StorageConfig.from(
                Env.from(mapOf("STORAGE_PROVIDER" to "GCS", "GCS_BUCKET_NAME" to "somiq-uploads"))
            )
        )
        assertEquals("https://storage.googleapis.com/somiq-uploads", storage.publicUrlPrefix)
    }

    @Test
    fun `test AppConfig from aggregates all subconfigs`() {
        val env = Env.load()
        val appConfig = AppConfig.from(env)
        assertEquals(AppEnv.LOCAL, appConfig.env)
        assertEquals(StorageConfig.from(env), appConfig.storage)
        assertEquals(DatabaseConfig.from(env), appConfig.database)
        assertEquals(JwtConfig.from(env), appConfig.jwt)
    }

    @Test
    fun `test koin resolution of AppConfig, StorageConfig, and MediaStorageService`() {
        val koinApp = org.koin.dsl.koinApplication {
            modules(com.ranjan.core.di.coreModule)
        }
        val appConfig = koinApp.koin.get<AppConfig>()
        val storageConfig = koinApp.koin.get<StorageConfig>()
        val dbConfig = koinApp.koin.get<DatabaseConfig>()
        val jwtConfig = koinApp.koin.get<JwtConfig>()

        assertEquals(appConfig.storage, storageConfig)
        assertEquals(appConfig.database, dbConfig)
        assertEquals(appConfig.jwt, jwtConfig)

        val mediaStorage = koinApp.koin.get<com.ranjan.core.storage.MediaStorageService>()
        kotlin.test.assertNotNull(mediaStorage)

        val database = koinApp.koin.get<org.jetbrains.exposed.sql.Database>()
        kotlin.test.assertNotNull(database)
    }

    @Test
    fun `test Env validation throws exception when required variable is missing`() {
        val ex = kotlin.test.assertFailsWith<IllegalStateException> {
            val env = Env.from(emptyMap())
            AppConfig.from(env)
        }
        assertTrue(ex.message!!.contains("Missing or invalid required configuration"))
        assertTrue(ex.message!!.contains("JWT_SECRET"))
        assertTrue(ex.message!!.contains("DB_DRIVER"))
        assertTrue(ex.message!!.contains("JWT_ACCESS_TOKEN_LIFETIME"))
    }
}

