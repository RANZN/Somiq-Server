package com.ranjan.data.sources.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaInitializer {
    fun init(database: Database) {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(*AllTables.toTypedArray())
        }
    }
}