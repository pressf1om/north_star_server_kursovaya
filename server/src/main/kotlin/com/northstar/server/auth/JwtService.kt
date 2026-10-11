package com.northstar.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.northstar.server.config.AppConfig
import com.northstar.server.plugins.UserPrincipal
import java.util.Date

object JwtService {
    private const val ISSUER = "north-star"
    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(AppConfig.jwtSecret)

    fun issue(userId: Int, username: String, status: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId.toString())
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("status", status)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + 7L * 24 * 60 * 60 * 1000))
            .sign(algorithm)
    }

    fun verifier() = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun userId(payload: com.auth0.jwt.interfaces.Payload): Int? =
        payload.getClaim("userId")?.asInt() ?: payload.subject?.toIntOrNull()

    fun principalFromToken(rawToken: String): UserPrincipal? =
        runCatching {
            val token = rawToken.removePrefix("Bearer").trim()
            val jwt = verifier().verify(token)
            val userId = userId(jwt) ?: return null
            val status = jwt.getClaim("status").asString() ?: UserStatus.PENDING
            UserPrincipal(userId, status)
        }.getOrNull()
}
