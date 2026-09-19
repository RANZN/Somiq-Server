package com.ranjan.core.di

import com.ranjan.core.config.AppConfig
import com.ranjan.core.db.DatabaseConfig
import com.ranjan.core.db.DatabaseFactory
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single<DatabaseConfig> { get<AppConfig>().database }
    single<Database> { DatabaseFactory.connect(get<DatabaseConfig>()) }
}
