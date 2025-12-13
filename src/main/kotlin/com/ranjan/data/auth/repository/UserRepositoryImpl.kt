package com.ranjan.data.auth.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.common.model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepositoryImpl(
    private val db: Database
) : UserRepository {
    override suspend fun findByEmail(email: String): User? = db.dbQuery {
        UserTable.selectAll().where { UserTable.email eq email }
            .map { toUser(it) }
            .singleOrNull()
    }

    override suspend fun findById(userId: UUID): User? = db.dbQuery {
        UserTable.selectAll().where { UserTable.userId eq userId }
            .map { toUser(it) }
            .singleOrNull()
    }

    override suspend fun isEmailExists(email: String): Boolean = db.dbQuery {
        !UserTable.selectAll().where { UserTable.email eq email }.empty()
    }

    override suspend fun isUsernameExists(username: String): Boolean = db.dbQuery {
        !UserTable.selectAll().where { UserTable.username eq username }.empty()
    }

    override suspend fun saveUser(user: User): User? = db.dbQuery {
        val insertStatement = UserTable.insert {
            it[userId] = user.userId
            it[email] = user.email
            it[name] = user.name
            it[username] = user.username
            it[profilePictureUrl] = user.profilePictureUrl
            it[bio] = user.bio
            it[password] = user.hashedPassword
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::toUser)
    }

    override suspend fun updateUser(user: User): User? = db.dbQuery {
        UserTable.update({ UserTable.userId eq user.userId }) {
            it[name] = user.name
            it[username] = user.username
            it[profilePictureUrl] = user.profilePictureUrl
            it[bio] = user.bio
        }
        findById(user.userId)
    }

    private fun toUser(row: ResultRow) = User(
        userId = row[UserTable.userId],
        name = row[UserTable.name],
        email = row[UserTable.email],
        username = row[UserTable.username],
        profilePictureUrl = row[UserTable.profilePictureUrl],
        bio = row[UserTable.bio],
        hashedPassword = row[UserTable.password]
    )
}