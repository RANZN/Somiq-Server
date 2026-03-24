package com.ranjan.domain.auth.services

import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.common.model.User

interface TokenProvider {
    fun createToken(user: User, deviceId: String): AuthToken
    fun getUserIdFromRefreshToken(refreshToken: String): String?
    fun getDeviceIdFromRefreshToken(refreshToken: String): String?
    /** Short-lived token allowing profile completion after phone OTP (signup flow). */
    fun createSignupToken(phone: String, deviceId: String): String
    /** Returns normalized phone digits if valid signup token, else null. */
    fun getPhoneFromSignupToken(token: String): String?
    /** Returns deviceId if valid signup token, else null. */
    fun getDeviceIdFromSignupToken(token: String): String?
}