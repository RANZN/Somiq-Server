package com.ranjan.core.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
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
            signupTokenLifetime = 15.minutes,
            realm = "custom-realm"
        )

        assertEquals("my-super-secret-key-for-testing", config.secret)
        assertEquals("custom-issuer", config.issuer)
        assertEquals("custom-audience", config.audience)
        assertEquals("custom-signup-audience", config.signupAudience)
        assertEquals(2.hours, config.accessTokenLifetime)
        assertEquals(15.minutes, config.signupTokenLifetime)
        assertEquals("custom-realm", config.realm)

        assertNotNull(config.algorithm)
        assertNotNull(config.verifier)
        assertNotNull(config.signupVerifier)
    }

    @Test
    fun `test JwtConfig throws exception when secret is blank`() {
        assertFailsWith<IllegalArgumentException> {
            JwtConfig(secret = "")
        }
        assertFailsWith<IllegalArgumentException> {
            JwtConfig(secret = "   ")
        }
    }

    @Test
    fun `test JwtConfig fromEnv provides safe dev default for local`() {
        val config = JwtConfig.fromEnv(AppEnv.LOCAL)
        assertEquals(JwtConfig.DEV_DEFAULT_SECRET, config.secret)
        assertEquals(JwtConfig.DEFAULT_ISSUER, config.issuer)
        assertEquals(JwtConfig.DEFAULT_AUDIENCE, config.audience)
        assertEquals(JwtConfig.DEFAULT_SIGNUP_AUDIENCE, config.signupAudience)
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
