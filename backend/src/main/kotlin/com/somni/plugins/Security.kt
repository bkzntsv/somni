package com.somni.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "default-secret-change-in-production"
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "somni-backend"
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "somni-app"
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "Somni API"

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
