package com.ranjan.data.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ranjan.data.util.TimeProvider
import com.ranjan.domain.auth.model.AuthToken
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.common.model.User
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object JwtConfig {
    const val NAME = "auth-jwt" //todo change all these constants to env variables
    const val SECRET = "your-super-secret-for-jwt"
    const val ISSUER = "your=issuer"
    const val AUDIENCE = "your-audience"

    object Claims {
        const val USER_ID = "userId"
        const val EMAIL = "email"
        const val NAME = "name"
    }

    object Lifetime {
        val access = 15.minutes
        val refresh = 7.days
    }
}

class JwtTokenProvider(
    private val timeProvider: TimeProvider
) : TokenProvider {

    override fun createToken(user: User): AuthToken {
        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken(user.userId.toString())

        return AuthToken(accessToken, refreshToken)
    }

    private fun generateAccessToken(user: User): String {
        val validity = Date(timeProvider.nowMillis() + JwtConfig.Lifetime.access.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(JwtConfig.ISSUER)
            .withAudience(JwtConfig.AUDIENCE)
            .withClaim(JwtConfig.Claims.USER_ID, user.userId.toString())
            .withClaim(JwtConfig.Claims.EMAIL, user.email)
            .withClaim(JwtConfig.Claims.NAME, user.name)
            .withExpiresAt(validity)
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))
    }

    private fun generateRefreshToken(userId: String): String {
        val validity = Date(timeProvider.nowMillis() + JwtConfig.Lifetime.refresh.inWholeMilliseconds)
        return JWT.create()
            .withIssuer(JwtConfig.ISSUER)
            .withAudience(JwtConfig.AUDIENCE)
            .withSubject(userId)
            .withExpiresAt(validity)
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))
    }

}