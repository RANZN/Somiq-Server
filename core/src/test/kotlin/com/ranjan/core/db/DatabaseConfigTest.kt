package com.ranjan.core.db

import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseConfigTest {

    @Test
    fun `test DatabaseConfig from loads from environment`() {
        val config = DatabaseConfig.from(com.ranjan.core.config.Env.load())
        assertEquals("org.h2.Driver", config.driver)
        assertEquals("jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1", config.url)
        assertEquals("root", config.user)
        assertEquals("", config.password)
        assertEquals(5, config.maxPoolSize)
    }

    @Test
    fun `test DatabaseConfig fromEnv throws when variable is missing`() {
        kotlin.test.assertFailsWith<IllegalStateException> {
            DatabaseConfig.from(com.ranjan.core.config.Env.from(emptyMap()))
        }
    }

    @Test
    fun `test DatabaseFactory connects to in-memory H2 database`() {
        val testConfig = DatabaseConfig(
            driver = "org.h2.Driver",
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            user = "sa",
            password = "",
            maxPoolSize = 2
        )

        val database = DatabaseFactory.connect(testConfig)
        assertNotNull(database)

        transaction(database) {
            val result = exec("SELECT 1") { rs ->
                rs.next()
                rs.getInt(1)
            }
            assertEquals(1, result)
        }
    }
}
