package com.somni.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            context.respondText("Somni Backend API v1.0.0", ContentType.Text.Plain)
        }

        get("/health") {
            context.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "healthy",
                    "version" to "1.0.0",
                ),
            )
        }

        route("/api/v1") {
            route("/sleep-sessions") {
                get {
                    context.respondText("GET sleep sessions - Not implemented", ContentType.Text.Plain)
                }
                post {
                    context.respondText("POST sleep session - Not implemented", ContentType.Text.Plain)
                }
            }
            route("/profile") {
                get {
                    context.respondText("GET profile - Not implemented", ContentType.Text.Plain)
                }
                put {
                    context.respondText("PUT profile - Not implemented", ContentType.Text.Plain)
                }
            }
        }
    }
}
