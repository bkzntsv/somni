// Docker build: only backend (no shared/KMP to avoid Kotlin/Native in Linux image)
rootProject.name = "somni"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":backend")
