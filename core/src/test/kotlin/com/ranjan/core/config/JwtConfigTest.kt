package com.ranjan.core.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JwtConfigTest {

    @Test
    fun `test JwtConfig constructor creates valid instance`() {
        val config = JwtConfig(
            secret = "my-super-secret-key-for-testing",
            issuer = "custom-issuer",
            audience = "custom-audience",
            signupAudience = "custom-signup-audience",
            accessTokenLifetime = 2.hours,
            refreshTokenLifetime = 14.days,
            signupTokenLifetime = 15.minutes,
            realm = "custom-realm"
        )

        assertEquals("my-super-secret-key-for-testing", config.secret)
        assertEquals("custom-issuer", config.issuer)
        assertEquals("custom-audience", config.audience)
        assertEquals("custom-signup-audience", config.signupAudience)
        assertEquals(2.hours, config.accessTokenLifetime)
        assertEquals(14.days, config.refreshTokenLifetime)
        assertEquals(15.minutes, config.signupTokenLifetime)
        assertEquals("custom-realm", config.realm)

        assertNotNull(config.algorithm)
        assertNotNull(config.verifier)
        assertNotNull(config.signupVerifier)

        assertEquals(2.hours.inWholeMilliseconds, config.accessTokenLifetimeMs)
        assertEquals(14.days.inWholeMilliseconds, config.refreshTokenLifetimeMs)
        assertEquals(15.minutes.inWholeMilliseconds, config.signupTokenLifetimeMs)
        assertEquals("auth-jwt", JwtConfig.NAME)
    }

    @Test
    fun `test JwtConfig throws exception when secret is blank`() {
        assertFailsWith<IllegalArgumentException> {
            JwtConfig(
                secret = "",
                issuer = "custom-issuer",
                audience = "custom-audience",
                signupAudience = "custom-signup",
                realm = "custom-realm",
                accessTokenLifetime = 1.hours,
                refreshTokenLifetime = 7.days,
                signupTokenLifetime = 30.minutes
            )
        }
        assertFailsWith<IllegalArgumentException> {
            JwtConfig(
                secret = "   ",
                issuer = "custom-issuer",
                audience = "custom-audience",
                signupAudience = "custom-signup",
                realm = "custom-realm",
                accessTokenLifetime = 1.hours,
                refreshTokenLifetime = 7.days,
                signupTokenLifetime = 30.minutes
            )
        }
    }

    @Test
    fun `test JwtConfig from loads from environment`() {
        val config = JwtConfig.from(Env.load())
        assertEquals("local-development-secret-key-minimum-32-bytes-long!", config.secret)
        assertEquals("somiq-server", config.issuer)
        assertEquals("somiq-app", config.audience)
        assertEquals("somiq-signup", config.signupAudience)
        assertEquals("auth-jwt", config.realm)
        assertEquals(1.hours, config.accessTokenLifetime)
        assertEquals(7.days, config.refreshTokenLifetime)
        assertEquals(30.minutes, config.signupTokenLifetime)
    }

    @Test
    fun `test JwtConfig fromEnv throws when variable is missing`() {
        assertFailsWith<IllegalStateException> {
            JwtConfig.from(Env.from(emptyMap()))
        }
    }

    @Test
    fun `test Claims constants match expected keys`() {
        assertEquals("userId", JwtConfig.Claims.USER_ID)
        assertEquals("name", JwtConfig.Claims.NAME)
        assertEquals("phone", JwtConfig.Claims.PHONE)
        assertEquals("publicUserId", JwtConfig.Claims.PUBLIC_USER_ID)
        assertEquals("deviceId", JwtConfig.Claims.DEVICE_ID)
        assertEquals("purpose", JwtConfig.Claims.PURPOSE)
        assertEquals("signup", JwtConfig.Claims.SIGNUP_PURPOSE)
    }
}
