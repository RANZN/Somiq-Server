package com.ranjan.domain.common.services

interface PasswordCipher {
    suspend fun hashPassword(password: String): String
    suspend fun verifyPassword(password: String, hashedPassword: String): Boolean
}