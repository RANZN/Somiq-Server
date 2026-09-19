package com.ranjan.domain.auth.services

import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.common.model.User

/**
 * Service responsible for issuing authenticated user token pairs (access and refresh).
 */
interface AuthTokenProvider {
    fun createToken(user: User, deviceId: String): AuthToken
}