package com.ranjan.core.config

import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JwtConfigTest {

    private fun jwtConfig(vararg overrides: Pair<String, String>): MapApplicationConfig {
        val values = linkedMapOf(
            "jwt.secret" to "local-development-secret-key-minimum-32-bytes-long!",
            "jwt.issuer" to "somiq-server",
            "jwt.audience" to "somiq-app",
            "jwt.signupAudience" to "somiq-signup",
            "jwt.realm" to "auth-jwt",
            "jwt.accessTokenLifetime" to "1h",
            "jwt.refreshTokenLifetime" to "7d",
            "jwt.signupTokenLifetime" to "30m"
        )
        values.putAll(overrides)
        return MapApplicationConfig(*values.toList().toTypedArray())
    }

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
        )

        assertEquals("my-super-secret-key-for-testing", config.secret)
        assertEquals("custom-issuer", config.issuer)
        assertEquals("custom-audience", config.audience)
        assertEquals("custom-signup-audience", config.signupAudience)
        assertEquals(2.hours, config.accessTokenLifetime)
        assertEquals(14.days, config.refreshTokenLifetime)
        assertEquals(15.minutes, config.signupTokenLifetime)

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
                accessTokenLifetime = 1.hours,
                refreshTokenLifetime = 7.days,
                signupTokenLifetime = 30.minutes
            )
        }
    }

    @Test
    fun `test JwtConfig from loads from application config`() {
        val config = JwtConfig.from(jwtConfig())
        assertEquals("local-development-secret-key-minimum-32-bytes-long!", config.secret)
        assertEquals("somiq-server", config.issuer)
        assertEquals("somiq-app", config.audience)
        assertEquals("somiq-signup", config.signupAudience)
        assertEquals(1.hours, config.accessTokenLifetime)
        assertEquals(7.days, config.refreshTokenLifetime)
        assertEquals(30.minutes, config.signupTokenLifetime)
    }

    @Test
    fun `test JwtConfig from throws when secret is missing`() {
        val ex = assertFailsWith<IllegalStateException> {
            JwtConfig.from(jwtConfig("jwt.secret" to ""))
        }
        assertTrue(ex.message!!.contains("jwt.secret"))
    }

    @Test
    fun `test JwtConfig from throws on invalid duration`() {
        val ex = assertFailsWith<IllegalStateException> {
            JwtConfig.from(jwtConfig("jwt.accessTokenLifetime" to "one hour"))
        }
        assertTrue(ex.message!!.contains("jwt.accessTokenLifetime"))
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
