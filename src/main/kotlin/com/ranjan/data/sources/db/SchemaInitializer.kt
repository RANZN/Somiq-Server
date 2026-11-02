package com.ranjan.data.sources.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaInitializer {
    /**
     * Creates any missing tables. With [fullSync], also adds missing columns to existing tables,
     * which scans every table's metadata and adds several seconds to startup.
     */
    fun init(database: Database, fullSync: Boolean = false) {
        transaction(database) {
            if (fullSync) {
                SchemaUtils.createMissingTablesAndColumns(*AllTables.toTypedArray())
            } else {
                SchemaUtils.create(*AllTables.toTypedArray())
            }
        }
    }
}
