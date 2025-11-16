package com.ranjan.data.auth.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.common.model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class UserRepositoryImpl(
    private val db: Database
) : UserRepository {
    override suspend fun findByEmail(email: String): User? = db.dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }
            .map { toUser(it) }
            .singleOrNull()
    }

    override suspend fun isEmailExists(email: String): Boolean = db.dbQuery {
        !UserTable.selectAll().where { UserTable.email eq email }.empty()
    }

    override suspend fun saveUser(user: User): User? = db.dbQuery {
        val insertStatement = UserTable.insert {
            it[userId] = user.userId
            it[email] = user.email
            it[name] = user.name
            it[password] = user.hashedPassword
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::toUser)
    }

    private fun toUser(row: ResultRow) = User(
        userId = row[UserTable.userId],
        name = row[UserTable.name],
        email = row[UserTable.email],
        hashedPassword = row[UserTable.password]
    )
}