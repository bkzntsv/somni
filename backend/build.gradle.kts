plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "com.somni.backend"
version = "1.0.0"

application {
    mainClass.set("com.somni.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.serializationJson)
    implementation(Dependencies.datetime)
    implementation(Dependencies.ktorServerCore)
    implementation(Dependencies.ktorServerNetty)
    implementation(Dependencies.ktorServerContentNegotiation)
    implementation(Dependencies.ktorServerAuth)
    implementation(Dependencies.ktorServerAuthJwt)
    implementation(Dependencies.ktorServerCors)
    implementation(Dependencies.ktorServerStatusPages)
    implementation(Dependencies.ktorServerRateLimit)
    implementation(Dependencies.ktorSerializationJson)
    implementation(Dependencies.koinCore)
    implementation(Dependencies.koinKtor)
    implementation(Dependencies.koinLogger)
    implementation(Dependencies.mongoDriverKotlin)
    implementation(Dependencies.jwtAuth)
    implementation(Dependencies.logback)
    testImplementation(Dependencies.kotlinTest)
    testImplementation(Dependencies.kotlinTestJunit)
    testImplementation(Dependencies.ktorServerTests)
    testImplementation(Dependencies.mockk)
}

tasks.test {
    useJUnitPlatform()
}
