package com.ranjan.domain.auth.repository

import com.ranjan.domain.common.model.User
import java.util.UUID

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(userId: UUID): User?
    suspend fun isEmailExists(email: String): Boolean
    suspend fun isUsernameExists(username: String): Boolean
    suspend fun saveUser(user: User): User?
    suspend fun updateUser(user: User): User?
}