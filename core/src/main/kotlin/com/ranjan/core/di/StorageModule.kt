package com.ranjan.core.di

import com.google.cloud.storage.Storage
import com.ranjan.core.config.AppConfig
import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.StorageConfig
import com.ranjan.core.storage.StorageProvider
import com.ranjan.core.storage.gcs.GcsClientFactory
import com.ranjan.core.storage.gcs.GcsMediaStorageService
import com.ranjan.core.storage.gcs.GcsStorageUrlResolver
import com.ranjan.core.storage.local.LocalMediaStorageService
import com.ranjan.core.storage.local.LocalStorageUrlResolver
import org.koin.dsl.module

val storageModule = module {
    single<StorageConfig> { get<AppConfig>().storage }

    single<Storage> { GcsClientFactory.create(get<StorageConfig>()) }

    single<MediaStorageService> {
        val storageConfig = get<StorageConfig>()
        when (storageConfig.provider) {
            StorageProvider.GCS -> {
                val urlResolver = GcsStorageUrlResolver(storageConfig.gcsPublicUrlPrefix)
                GcsMediaStorageService(
                    bucketName = storageConfig.gcsBucket,
                    storage = get(),
                    urlResolver = urlResolver,
                    mimeTypeResolver = get(),
                    fileFormatDetector = get()
                )
            }

            StorageProvider.LOCAL -> {
                val urlResolver = LocalStorageUrlResolver()
                LocalMediaStorageService(
                    urlResolver = urlResolver,
                    fileFormatDetector = get()
                )
            }
        }
    }
}