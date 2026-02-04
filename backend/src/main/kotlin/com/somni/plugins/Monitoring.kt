package com.somni.plugins

import io.ktor.server.application.Application

fun Application.configureMonitoring() {
    // Metrics and tracing will be wired here (Prometheus/OpenTelemetry).
}
