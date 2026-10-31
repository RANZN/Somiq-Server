package com.ranjan.core.config

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.getAs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DatabaseConfigTest {

    @Test
    fun `test DatabaseConfig decodes from config section`() {
        val config = MapApplicationConfig(
            "database.url" to "jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1",
            "database.user" to "root",
            "database.maxPoolSize" to "5"
        ).property("database").getAs<DatabaseConfig>()

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
    fun `test DatabaseConfig rejects blank url`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            DatabaseConfig(url = "", user = "root")
        }
        assertTrue(ex.message!!.contains("DB_URL"))
    }

    @Test
    fun `test DatabaseConfig rejects unsupported url`() {
        assertFailsWith<IllegalArgumentException> {
            DatabaseConfig(url = "jdbc:mysql://host/db", user = "root")
        }
    }
}
