package com.ranjan.domain.auth.repository

import com.ranjan.domain.auth.model.RefreshTokenEntity

interface RefreshTokenRepo {
    suspend fun save(userId: String, refreshToken: String) : RefreshTokenEntity?
    suspend fun findByToken(token: String): Boolean
    suspend fun deleteByUserId(userId: String)
    suspend fun deleteByToken(token: String) : Int
}