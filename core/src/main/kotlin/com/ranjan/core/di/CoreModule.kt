package com.ranjan.core.di

import com.ranjan.core.config.AppConfig
import com.ranjan.core.config.CorsConfig
import com.ranjan.core.config.Env
import com.ranjan.core.config.JwtConfig
import com.ranjan.core.util.*
import com.ranjan.core.util.impl.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    includes(databaseModule, storageModule)

    single<Env> { Env.load() }
    single<AppConfig> { AppConfig.from(get()) }
    single<CorsConfig> { get<AppConfig>().cors }
    single<JwtConfig> { get<AppConfig>().jwt }

    singleOf(::SystemTimeProvider) bind TimeProvider::class
    singleOf(::DefaultMimeTypeResolver) bind MimeTypeResolver::class
    singleOf(::MagicByteFileFormatDetector) bind FileFormatDetector::class
}
