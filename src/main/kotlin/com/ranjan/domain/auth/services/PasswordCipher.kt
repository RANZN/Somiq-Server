package com.ranjan.domain.auth.services

interface PasswordCipher {
    suspend fun hashPassword(password: String): String
    suspend fun verifyPassword(password: String, hashedPassword: String): Boolean
}