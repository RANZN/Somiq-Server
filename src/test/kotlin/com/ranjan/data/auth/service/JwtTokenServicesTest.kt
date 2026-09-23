package com.ranjan.data.auth.service

import com.ranjan.core.config.JwtConfig
import com.ranjan.core.util.TimeProvider
import com.ranjan.domain.common.model.User
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class JwtTokenServicesTest {

    private class TestTimeProvider(var time: Long = System.currentTimeMillis()) : TimeProvider {
        override fun nowMillis(): Long = time
        override fun now(): kotlinx.datetime.Instant = kotlinx.datetime.Instant.fromEpochMilliseconds(time)
    }

    private val jwtConfig = JwtConfig(
        secret = "test-secret-at-least-32-bytes-long-for-hmac256!",
        issuer = "test-issuer",
        audience = "test-audience",
        signupAudience = "test-signup-audience",
        realm = "test-realm",
        accessTokenLifetime = 1.hours,
        refreshTokenLifetime = 7.minutes,
        signupTokenLifetime = 30.seconds
    )

    private val timeProvider = TestTimeProvider()
    private val authTokenService = JwtAuthTokenService(timeProvider, jwtConfig)
    private val refreshTokenService = JwtRefreshTokenService(timeProvider, jwtConfig)
    private val signupTokenService = JwtSignupTokenService(timeProvider, jwtConfig)

    private val testUser = User(
        userId = UUID.randomUUID(),
        name = "Test User",
        phone = "+1234567890",
        username = "testuser"
    )

    @Test
    fun `test createToken creates valid access and refresh tokens`() {
        val deviceId = "device-abc-123"
        val authToken = authTokenService.createToken(testUser, deviceId)

        assertNotNull(authToken.accessToken)
        assertNotNull(authToken.refreshToken)

        val claims = refreshTokenService.parseRefreshToken(authToken.refreshToken)
        assertNotNull(claims)
        assertEquals(testUser.userId.toString(), claims.userId)
        assertEquals(deviceId, claims.deviceId)
    }

    @Test
    fun `test created tokens contain issuedAt, expiresAt, and subject claims`() {
        val authToken = authTokenService.createToken(testUser, "device-iat-test")
        val decodedAccess = com.auth0.jwt.JWT.decode(authToken.accessToken)
        val decodedRefresh = com.auth0.jwt.JWT.decode(authToken.refreshToken)

        assertNotNull(decodedAccess.issuedAt)
        assertNotNull(decodedRefresh.issuedAt)
        assertEquals(testUser.userId.toString(), decodedAccess.subject)
        assertEquals(testUser.userId.toString(), decodedRefresh.subject)
    }

    @Test
    fun `test parseRefreshToken returns structured claims`() {
        val deviceId = "device-claims-123"
        val authToken = authTokenService.createToken(testUser, deviceId)

        val claims = refreshTokenService.parseRefreshToken(authToken.refreshToken)
        assertNotNull(claims)
        assertEquals(testUser.userId.toString(), claims.userId)
        assertEquals(deviceId, claims.deviceId)
    }

    @Test
    fun `test consecutive parseRefreshToken calls return consistent results`() {
        val deviceId = "device-consistent"
        val authToken = authTokenService.createToken(testUser, deviceId)

        val claims1 = refreshTokenService.parseRefreshToken(authToken.refreshToken)
        val claims2 = refreshTokenService.parseRefreshToken(authToken.refreshToken)

        assertNotNull(claims1)
        assertNotNull(claims2)
        assertEquals(claims1, claims2)
        assertEquals(testUser.userId.toString(), claims1.userId)
        assertEquals(deviceId, claims1.deviceId)
    }

    @Test
    fun `test expired refresh token returns null`() {
        val deviceId = "device-expire-test"
        val authToken = authTokenService.createToken(testUser, deviceId)

        // Advance time past 7 minutes (refreshTokenLifetime)
        timeProvider.time += 8.minutes.inWholeMilliseconds

        assertNull(refreshTokenService.parseRefreshToken(authToken.refreshToken))
    }

    @Test
    fun `test invalid refresh token returns null`() {
        assertNull(refreshTokenService.parseRefreshToken("invalid.jwt.token"))
        assertNull(refreshTokenService.parseRefreshToken("not-a-token"))
    }

    @Test
    fun `test createSignupToken and parseSignupToken`() {
        val phone = "+9876543210"
        val deviceId = "signup-device-456"

        val signupToken = signupTokenService.createSignupToken(phone, deviceId)
        assertNotNull(signupToken)

        val claims = signupTokenService.parseSignupToken(signupToken)
        assertNotNull(claims)
        assertEquals(phone, claims.phone)
        assertEquals(deviceId, claims.deviceId)
    }

    @Test
    fun `test expired signup token returns null`() {
        val signupToken = signupTokenService.createSignupToken("+1112223333", "device-expire")

        // Advance time past 30 seconds (signupTokenLifetime)
        timeProvider.time += 35.seconds.inWholeMilliseconds

        assertNull(signupTokenService.parseSignupToken(signupToken))
    }

    @Test
    fun `test invalid signup token returns null`() {
        assertNull(signupTokenService.parseSignupToken("invalid.token"))
        assertNull(signupTokenService.parseSignupToken("not-a-token"))
    }

    @Test
    fun `test refresh token cannot be used as signup token`() {
        val authToken = authTokenService.createToken(testUser, "device-mix")

        // Passing refresh token into signup token verification should fail
        assertNull(signupTokenService.parseSignupToken(authToken.refreshToken))
    }
}
