plugins {
    kotlin("multiplatform") version "1.9.22" apply false
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("com.android.library") version "8.2.2" apply false
    id("app.cash.sqldelight") version "2.2.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0" apply false
    id("org.openapi.generator") version "7.2.0" apply false
}

allprojects {
    group = "com.somni"
    version = "1.0.0"
}

subprojects {
    // Only backend: shared has SQLDelight-generated code that ktlint flags (filter doesn't exclude it in KMP)
    if (name == "backend") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
