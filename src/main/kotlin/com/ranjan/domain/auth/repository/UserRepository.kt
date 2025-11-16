package com.ranjan.domain.auth.repository

import com.ranjan.domain.common.model.User

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun isEmailExists(email: String): Boolean
    suspend fun saveUser(user: User): User?
}