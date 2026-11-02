package com.ranjan.data.sources.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaInitializer {
    /**
     * Creates any tables that do not exist yet. Existing tables are left unchanged,
     * so new columns on an existing table must be added to the database by hand.
     */
    fun init(database: Database) {
        transaction(database) {
            SchemaUtils.create(*AllTables.toTypedArray())
        }
    }
}
