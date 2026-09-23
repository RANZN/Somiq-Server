package com.ranjan.data.auth.service

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.ranjan.core.config.JwtConfig
import com.ranjan.core.config.JwtConfig.Claims
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.auth.model.RefreshTokenClaims
import com.ranjan.domain.auth.services.RefreshTokenService
import org.slf4j.LoggerFactory

class JwtRefreshTokenService(
    private val timeProvider: TimeProvider,
    private val jwtConfig: JwtConfig,
) : RefreshTokenService {

    private val logger = LoggerFactory.getLogger(JwtRefreshTokenService::class.java)

    override fun parseRefreshToken(refreshToken: String): RefreshTokenClaims? {
        val jwt = verifyRefreshToken(refreshToken) ?: return null
        val userId = jwt.subject ?: return null
        val deviceId = jwt.getClaim(Claims.DEVICE_ID).asString() ?: return null
        return RefreshTokenClaims(userId = userId, deviceId = deviceId)
    }

    private fun verifyRefreshToken(token: String): DecodedJWT? {
        return try {
            val jwt = jwtConfig.verifier.verify(token)
            val now = timeProvider.nowMillis()
            val expiresAt = jwt.expiresAt?.time
            if (expiresAt != null && expiresAt <= now) {
                logger.debug("Refresh token expired: expiresAt={}, now={}", expiresAt, now)
                return null
            }
            jwt
        } catch (e: JWTVerificationException) {
            logger.debug("Refresh token verification failed: {}", e.message)
            null
        } catch (e: Exception) {
            logger.warn("Unexpected error verifying refresh token: {}", e.message)
            null
        }
    }
}
