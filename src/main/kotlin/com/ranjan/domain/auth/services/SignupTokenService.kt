package com.ranjan.domain.auth.services

import com.ranjan.domain.auth.model.SignupTokenClaims

/**
 * Service responsible for creating and validating pre-registration signup tokens.
 */
interface SignupTokenService {
    /** Short-lived token allowing profile completion after phone OTP (signup flow). */
    fun createSignupToken(phone: String, deviceId: String): String

    fun parseSignupToken(token: String): SignupTokenClaims?
}
