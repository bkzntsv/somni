package com.somni.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

private fun Application.jwtConfig(
    path: String,
    default: String,
): String = environment.config.propertyOrNull(path)?.getString() ?: default

fun Application.configureSecurity() {
    val secret = jwtConfig("jwt.secret", "default-secret-change-in-production")
    val issuer = jwtConfig("jwt.issuer", "somni-backend")
    val audience = jwtConfig("jwt.audience", "somni-app")
    val realm = jwtConfig("jwt.realm", "Somni API")

    authentication {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build(),
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId == null) return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}
