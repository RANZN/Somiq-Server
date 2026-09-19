package com.ranjan.core.config

import com.ranjan.core.storage.StorageConfig
import com.ranjan.core.storage.StorageProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppConfigTest {

    @Test
    fun `test AppEnv fromString parsing and defaults`() {
        assertEquals(AppEnv.LOCAL, AppEnv.fromString("LOCAL"))
        assertEquals(AppEnv.LOCAL, AppEnv.fromString("local"))
        assertEquals(AppEnv.STAGING, AppEnv.fromString("STAGING"))
        assertEquals(AppEnv.STAGING, AppEnv.fromString("staging"))
        assertEquals(AppEnv.PRODUCTION, AppEnv.fromString("PRODUCTION"))
        assertEquals(AppEnv.PRODUCTION, AppEnv.fromString("production"))

        // Fallback default
        assertEquals(AppEnv.STAGING, AppEnv.fromString("UNKNOWN"))
        assertEquals(AppEnv.STAGING, AppEnv.fromString(null))
        assertEquals(AppEnv.STAGING, AppEnv.fromString(""))
    }

    @Test
    fun `test AppEnv isProduction flag`() {
        assertTrue(AppEnv.PRODUCTION.isProduction)
        assertFalse(AppEnv.STAGING.isProduction)
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
    fun `test StorageProvider fromString parsing and fallback`() {
        assertEquals(StorageProvider.GCS, StorageProvider.fromString("gcs"))
        assertEquals(StorageProvider.GCS, StorageProvider.fromString("GCS"))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString("local"))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString("LOCAL"))

        // When null or unknown, fallback depends on AppEnv
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString(null, AppEnv.LOCAL))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString("unknown", AppEnv.LOCAL))
        assertEquals(StorageProvider.LOCAL, StorageProvider.fromString(null, AppEnv.STAGING))
        assertEquals(StorageProvider.GCS, StorageProvider.fromString(null, AppEnv.PRODUCTION))
        assertEquals(StorageProvider.GCS, StorageProvider.fromString("unknown", AppEnv.PRODUCTION))
    }

    @Test
    fun `test StorageProvider helper properties`() {
        assertTrue(StorageProvider.GCS.isGcs)
        assertFalse(StorageProvider.GCS.isLocal)

        assertTrue(StorageProvider.LOCAL.isLocal)
        assertFalse(StorageProvider.LOCAL.isGcs)
    }

    @Test
    fun `test AppConfig determineStorageConfig`() {
        val storage = AppConfig.determineStorageConfig(AppEnv.LOCAL)
        assertTrue(storage.isLocal || storage.isGcs)
        assertEquals(storage.gcsBucket, storage.gcsBucket)
    }

    @Test
    fun `test koin resolution of AppConfig, StorageConfig, and MediaStorageService`() {
        val koinApp = org.koin.dsl.koinApplication {
            modules(
                com.ranjan.core.di.coreModule,
                com.ranjan.core.di.storageModule,
                com.ranjan.core.di.databaseModule
            )
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
}

