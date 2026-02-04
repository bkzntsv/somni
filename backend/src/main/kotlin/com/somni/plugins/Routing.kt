package com.somni.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

private const val API_VERSION = "1.0.0"

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Somni Backend API v$API_VERSION", ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "healthy",
                    "version" to API_VERSION,
                ),
            )
        }

        route("/api/v1") {
            route("/sleep-sessions") {
                get { call.respondNotImplemented("GET sleep sessions") }
                post { call.respondNotImplemented("POST sleep session") }
            }
            route("/profile") {
                get { call.respondNotImplemented("GET profile") }
                put { call.respondNotImplemented("PUT profile") }
            }
        }
    }
}

private suspend fun ApplicationCall.respondNotImplemented(message: String) {
    respondText("$message - Not implemented", ContentType.Text.Plain)
}
