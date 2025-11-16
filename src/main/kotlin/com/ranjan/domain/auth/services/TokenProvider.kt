package com.ranjan.domain.auth.services

import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.common.model.User

interface TokenProvider {
    fun createToken(user: User): AuthToken
}