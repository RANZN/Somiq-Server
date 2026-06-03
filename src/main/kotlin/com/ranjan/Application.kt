package com.ranjan

import com.ranjan.core.config.configureCORS
import com.ranjan.core.config.configureExceptionHandling
import com.ranjan.core.config.configureSecurity
import com.ranjan.core.config.configureSerialization
import com.ranjan.core.di.databaseModule
import com.ranjan.data.di.dataModule
import com.ranjan.data.sources.db.SchemaInitializer
import com.ranjan.domain.di.domainModule
import com.ranjan.server.configureRoutes
import com.ranjan.server.di.appModule
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    configureKoin()
    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureRoutes()
    configureExceptionHandling()
    configureCORS()
}

fun Application.configureKoin() {
    install(Koin) {
        printLogger()
        modules(databaseModule, dataModule, domainModule, appModule)
    }
}

fun Application.configureDatabase() {
    val database: Database by inject()
    SchemaInitializer.init(database)
}
