package com.ranjan.data.sources.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DbConfig {
    val DRIVER: String = System.getenv("DB_DRIVER") ?: "org.h2.Driver"
    val URL: String = System.getenv("DB_URL") ?: "jdbc:h2:file:./build/db"
    val USER: String = System.getenv("DB_USER") ?: "root"
    val PASSWORD: String = System.getenv("DB_PASSWORD") ?: ""
}

object DataSourceProvider {
    fun initDatabase(): Database {
        val dataSource = createHikariDataSource(DbConfig.URL, DbConfig.DRIVER, DbConfig.USER, DbConfig.PASSWORD)
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.create(*AllTables.toTypedArray())
        }
        return database
    }

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
}