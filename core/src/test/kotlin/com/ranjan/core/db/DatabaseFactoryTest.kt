package com.ranjan.core.db

import com.ranjan.core.config.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseFactoryTest {

    @Test
    fun `test DatabaseFactory connects to in-memory H2 database`() {
        val testConfig = DatabaseConfig(
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
