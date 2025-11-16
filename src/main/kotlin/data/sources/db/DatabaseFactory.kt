package com.ranjan.data.db

import com.ranjan.data.auth.model.UserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DbConfig {
    val DRIVER: String = System.getenv("DB_DRIVER") ?: "org.h2.Driver"
    val URL: String = System.getenv("DB_URL") ?: "jdbc:h2:file:./build/db"
    val USER: String = System.getenv("DB_USER") ?: "root"
    val PASSWORD: String = System.getenv("DB_PASSWORD") ?: ""
}

object DatabaseFactory {
    fun init() {
        Database.connect(createHikariDataSource(DbConfig.URL, DbConfig.DRIVER, DbConfig.USER, DbConfig.PASSWORD))

        // Create database tables
        transaction {
            SchemaUtils.create(UserTable)
        }
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

    /**
     * This is the helper function you asked about.
     * It runs the database query block on a dedicated IO thread pool.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}