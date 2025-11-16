package com.ranjan.data.repository

import com.ranjan.data.db.DatabaseFactory.dbQuery
import com.ranjan.data.db.UserTable
import com.ranjan.domain.common.model.User
import com.ranjan.domain.auth.repository.UserRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class UserRepositoryImpl : UserRepository {
    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }
            .map { toUser(it) }
            .singleOrNull()
    }

    override suspend fun isEmailExists(email: String): Boolean = dbQuery{
        !UserTable.selectAll().where { UserTable.email eq email }.empty()
    }

    override suspend fun saveUser(user: User): User? = dbQuery {
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