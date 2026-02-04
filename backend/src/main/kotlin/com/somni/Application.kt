package com.somni

import com.somni.plugins.configureMonitoring
import com.somni.plugins.configureRouting
import com.somni.plugins.configureSecurity
import com.somni.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
