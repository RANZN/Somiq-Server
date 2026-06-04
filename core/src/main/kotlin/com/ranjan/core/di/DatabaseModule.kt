package com.ranjan.core.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single<Database> {
        val dbConfig = get<DbConfig>()
        val dataSource = createHikariDataSource(dbConfig.url, dbConfig.driver, dbConfig.user, dbConfig.password)
        Database.connect(dataSource)
    }
    single<DbConfig> {
        DbConfig(
            driver = System.getenv("DB_DRIVER") ?: "org.h2.Driver",
            url = System.getenv("DB_URL") ?: "jdbc:h2:file:./build/db",
            user = System.getenv("DB_USER") ?: "root",
            password = System.getenv("DB_PASSWORD") ?: ""
        )
    }
}

private data class DbConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String
)

private fun createHikariDataSource(
    url: String,
    driver: String,
    user: String,
    password: String
) = HikariDataSource(HikariConfig().apply {
    driverClassName = driver
    jdbcUrl = url
    username = user
    this.password = password
    maximumPoolSize = 3
    isAutoCommit = false
    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    validate()
})
