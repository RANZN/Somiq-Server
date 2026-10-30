package com.ranjan.core.di

import com.ranjan.core.config.AppConfig
import com.ranjan.core.config.StorageConfig
import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.gcs.GcsClientFactory
import com.ranjan.core.storage.gcs.GcsMediaStorageService
import com.ranjan.core.storage.gcs.GcsStorageUrlResolver
import com.ranjan.core.storage.local.LocalMediaStorageService
import com.ranjan.core.storage.local.LocalStorageUrlResolver
import com.ranjan.core.storage.stratus.StratusMediaStorageService
import com.ranjan.core.storage.stratus.StratusStorageUrlResolver
import com.ranjan.core.storage.stratus.ZohoTokenProvider
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

private val StorageHttpClient = named("storage")
val storageModule = module {
    single<StorageConfig> { get<AppConfig>().storage }

    single<HttpClient>(StorageHttpClient) { HttpClient(CIO) } onClose { it?.close() }

    single<MediaStorageService> {
        when (val config = get<StorageConfig>()) {
            is StorageConfig.Stratus -> StratusMediaStorageService(
                bucketUrl = config.bucketUrl,
                httpClient = get(StorageHttpClient),
                tokenProvider = ZohoTokenProvider(get(), config.oauth),
                urlResolver = StratusStorageUrlResolver(config.bucketUrl),
                mimeTypeResolver = get(),
                fileFormatDetector = get()
            )

            is StorageConfig.Gcs -> GcsMediaStorageService(
                bucketName = config.bucket,
                storage = GcsClientFactory.create(config),
                urlResolver = GcsStorageUrlResolver(config.publicUrlPrefix),
                mimeTypeResolver = get(),
                fileFormatDetector = get()
            )

            StorageConfig.Local -> LocalMediaStorageService(
                urlResolver = LocalStorageUrlResolver(),
                fileFormatDetector = get()
            )
        }
    }
}