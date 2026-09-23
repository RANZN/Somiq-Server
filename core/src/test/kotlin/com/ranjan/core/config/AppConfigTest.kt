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
        assertEquals(listOf(StorageProvider.LOCAL, StorageProvider.GCS), StorageProvider.entries)
    }

    @Test
    fun `test StorageConfig from loads from Env`() {
        val storage = StorageConfig.from(Env.load())
        assertEquals(StorageProvider.LOCAL, storage.provider)
        assertEquals("somiq-uploads", storage.gcsBucket)
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
        assertTrue(ex.message!!.contains("Missing or invalid required environment variables"))
        assertTrue(ex.message!!.contains("JWT_SECRET"))
        assertTrue(ex.message!!.contains("DB_DRIVER"))
        assertTrue(ex.message!!.contains("JWT_ACCESS_TOKEN_LIFETIME"))
    }
}

