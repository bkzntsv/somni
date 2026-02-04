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
            call.respondText("Somni Backend API v1.0.0", ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(
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
                    call.respondText("GET sleep sessions - Not implemented", ContentType.Text.Plain)
                }
                post {
                    call.respondText("POST sleep session - Not implemented", ContentType.Text.Plain)
                }
            }
            route("/profile") {
                get {
                    call.respondText("GET profile - Not implemented", ContentType.Text.Plain)
                }
                put {
                    call.respondText("PUT profile - Not implemented", ContentType.Text.Plain)
                }
            }
        }
    }
}
