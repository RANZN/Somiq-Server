package com.ranjan.data.auth.repository

import com.ranjan.data.auth.model.RefreshTokenTable
import com.ranjan.data.auth.service.JwtConfig
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.domain.auth.model.RefreshTokenEntity
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class RefreshTokenRepoImpl(
    private val db: Database
) : RefreshTokenRepo {

    override suspend fun save(userId: String, refreshToken: String): RefreshTokenEntity? = db.dbQuery {
        val expiry = Clock.System.now().plus(JwtConfig.Lifetime.refresh)
        val insertStatement = RefreshTokenTable.insert {
            it[this.userId] = userId
            it[this.token] = refreshToken
            it[this.expiresAt] = expiry
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::toRefreshTokenEntity)
    }

    override suspend fun findByToken(token: String): Boolean {
        return RefreshTokenTable.selectAll().where {
            RefreshTokenTable.token eq token
        }.empty().not()
    }

    override suspend fun deleteByUserId(userId: String) {
        transaction {
            RefreshTokenTable.deleteWhere { this.userId eq userId }
        }
    }

    override suspend fun deleteByToken(token: String): Int {
        return transaction {
            RefreshTokenTable.deleteWhere { this.token eq token }
        }
    }

    private fun toRefreshTokenEntity(row: ResultRow): RefreshTokenEntity {
        return RefreshTokenEntity(
            id = row[RefreshTokenTable.id].toString(),
            userId = row[RefreshTokenTable.userId],
            token = row[RefreshTokenTable.token],
            expiresAt = row[RefreshTokenTable.expiresAt],
            createdAt = row[RefreshTokenTable.createdAt]
        )
    }
}