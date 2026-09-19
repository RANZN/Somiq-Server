package com.ranjan.core.config

import com.ranjan.core.db.DatabaseConfig
import com.ranjan.core.storage.StorageConfig
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
        assertFalse(AppEnv.PRODUCTION.isLocal)
        assertTrue(AppEnv.LOCAL.isLocal)
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
    fun `test StorageProvider fromString parsing and validation`() {
        assertEquals(StorageProvider.GCS, StorageProvider.fromString("gcs"))
        assertEquals(StorageProvider.GCS, StorageProvider.fromString("GCS"))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString("local"))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString("LOCAL"))

        // Throws on unknown or missing
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            StorageProvider.fromString(null)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            StorageProvider.fromString("unknown")
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            StorageProvider.fromString("")
        }
    }

    @Test
    fun `test StorageProvider helper properties`() {
        assertTrue(StorageProvider.GCS.isGcs)
        assertFalse(StorageProvider.GCS.isLocal)

        assertTrue(StorageProvider.LOCAL.isLocal)
        assertFalse(StorageProvider.LOCAL.isGcs)
    }

    @Test
    fun `test StorageConfig get loads from Env`() {
        val storage = StorageConfig.get()
        assertTrue(storage.provider.isLocal || storage.provider.isGcs)
        assertEquals(Env.gcsBucketName, storage.gcsBucket)
    }

    @Test
    fun `test AppConfig load aggregates all subconfigs`() {
        val appConfig = AppConfig.load()
        assertEquals(Env.appEnv, appConfig.env)
        assertEquals(StorageConfig.get(), appConfig.storage)
        assertEquals(DatabaseConfig.get(), appConfig.database)
        assertEquals(JwtConfig.get(), appConfig.jwt)
        assertEquals(appConfig.storage.gcsBucket, appConfig.bucketName)
        assertEquals(appConfig.database.url, appConfig.dbUrl)
    }

    @Test
    fun `test koin resolution of AppConfig, StorageConfig, and MediaStorageService`() {
        val koinApp = org.koin.dsl.koinApplication {
            modules(com.ranjan.core.di.coreModule)
        }
        val appConfig = koinApp.koin.get<AppConfig>()
        val storageConfig = koinApp.koin.get<StorageConfig>()
        val dbConfig = koinApp.koin.get<com.ranjan.core.db.DatabaseConfig>()
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
            Env.from(emptyMap())
        }
        assertTrue(ex.message!!.contains("Missing or invalid required environment variables"))
        assertTrue(ex.message!!.contains("JWT_SECRET"))
        assertTrue(ex.message!!.contains("DB_DRIVER"))
        assertTrue(ex.message!!.contains("JWT_ACCESS_TOKEN_LIFETIME"))
    }
}

