package com.ranjan.core.db

import com.ranjan.core.config.AppEnv
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseConfigTest {

    @Test
    fun `test DatabaseConfig fromEnv defaults for local and production`() {
        val localConfig = DatabaseConfig.fromEnv(AppEnv.LOCAL)
        assertEquals("org.h2.Driver", localConfig.driver)
        assertEquals(5, localConfig.maxPoolSize)

        val prodConfig = DatabaseConfig.fromEnv(AppEnv.PRODUCTION)
        assertEquals("org.postgresql.Driver", prodConfig.driver)
        assertEquals(10, prodConfig.maxPoolSize)
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
