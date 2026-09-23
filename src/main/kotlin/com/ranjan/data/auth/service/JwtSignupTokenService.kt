package com.ranjan.data.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.ranjan.core.config.JwtConfig
import com.ranjan.core.config.JwtConfig.Claims
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.auth.model.SignupTokenClaims
import com.ranjan.domain.auth.services.SignupTokenService
import org.slf4j.LoggerFactory
import java.util.Date

class JwtSignupTokenService(
    private val timeProvider: TimeProvider,
    private val jwtConfig: JwtConfig,
) : SignupTokenService {

    private val logger = LoggerFactory.getLogger(JwtSignupTokenService::class.java)

    override fun createSignupToken(phone: String, deviceId: String): String {
        val now = timeProvider.nowMillis()
        val issuedAt = Date(now)
        val validity = Date(now + jwtConfig.signupTokenLifetimeMs)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.signupAudience)
            .withIssuedAt(issuedAt)
            .withClaim(Claims.PURPOSE, Claims.SIGNUP_PURPOSE)
            .withClaim(Claims.PHONE, phone)
            .withClaim(Claims.DEVICE_ID, deviceId)
            .withExpiresAt(validity)
            .sign(jwtConfig.algorithm)
    }

    override fun parseSignupToken(token: String): SignupTokenClaims? {
        val jwt = verifySignupToken(token) ?: return null
        val phone = jwt.getClaim(Claims.PHONE).asString() ?: return null
        val deviceId = jwt.getClaim(Claims.DEVICE_ID).asString() ?: return null
        return SignupTokenClaims(phone = phone, deviceId = deviceId)
    }

    private fun verifySignupToken(token: String): DecodedJWT? {
        return try {
            val jwt = jwtConfig.signupVerifier.verify(token)
            val now = timeProvider.nowMillis()
            val expiresAt = jwt.expiresAt?.time
            if (expiresAt != null && expiresAt <= now) {
                logger.debug("Signup token expired: expiresAt={}, now={}", expiresAt, now)
                return null
            }
            jwt
        } catch (e: JWTVerificationException) {
            logger.debug("Signup token verification failed: {}", e.message)
            null
        } catch (e: Exception) {
            logger.warn("Unexpected error verifying signup token: {}", e.message)
            null
        }
    }
}
