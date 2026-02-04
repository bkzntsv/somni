package com.somni.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Somni Backend API v1.0.0", ContentType.Text.Plain)
        }
        
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf(
                "status" to "healthy",
                "version" to "1.0.0"
            ))
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
