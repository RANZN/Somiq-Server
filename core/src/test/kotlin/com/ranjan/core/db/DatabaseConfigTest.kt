package com.ranjan.core.db

import com.ranjan.core.config.DatabaseConfig
import io.ktor.server.config.MapApplicationConfig
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseConfigTest {

    @Test
    fun `test DatabaseConfig from loads from application config`() {
        val config = DatabaseConfig.from(
            MapApplicationConfig(
                "database.url" to "jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1",
                "database.user" to "root",
                "database.maxPoolSize" to "5"
            )
        )
        assertEquals("org.h2.Driver", config.driver)
        assertEquals("jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1", config.url)
        assertEquals("root", config.user)
        assertEquals("", config.password)
        assertEquals(5, config.maxPoolSize)
    }

    @Test
    fun `test DatabaseConfig derives postgres driver from url`() {
        assertEquals("org.postgresql.Driver", DatabaseConfig.driverFor("jdbc:postgresql://host:5432/db"))
    }

    @Test
    fun `test DatabaseConfig from throws when url is missing`() {
        val ex = assertFailsWith<IllegalStateException> {
            DatabaseConfig.from(MapApplicationConfig("database.user" to "root"))
        }
        assertTrue(ex.message!!.contains("database.url"))
    }

    @Test
    fun `test DatabaseConfig rejects unsupported url`() {
        assertFailsWith<IllegalStateException> {
            DatabaseConfig.driverFor("jdbc:mysql://host/db")
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