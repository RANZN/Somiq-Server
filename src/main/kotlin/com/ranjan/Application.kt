package com.ranjan

import com.ranjan.core.config.AppConfig
import com.ranjan.core.di.coreModule
import com.ranjan.core.di.databaseModule
import com.ranjan.core.di.storageModule
import com.ranjan.core.plugin.configureCORS
import com.ranjan.core.plugin.configureExceptionHandling
import com.ranjan.core.plugin.configureSecurity
import com.ranjan.core.plugin.configureSerialization
import com.ranjan.data.di.dataModule
import com.ranjan.data.sources.db.SchemaInitializer
import com.ranjan.domain.di.domainModule
import com.ranjan.server.configureRoutes
import com.ranjan.server.di.appModule
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    configureKoin()
    val config: AppConfig by inject()
    configureDatabase()
    configureSerialization()
    configureSecurity(config.jwt)
    configureRoutes()
    configureExceptionHandling()
    configureCORS(config.cors)
}

fun Application.configureKoin() {
    install(Koin) {
        printLogger()
        modules(coreModule, databaseModule, storageModule, dataModule, domainModule, appModule)
    }
}

fun Application.configureDatabase() {
    val database: Database by inject()
    SchemaInitializer.init(database)
}
