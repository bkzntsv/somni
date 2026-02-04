plugins {
    kotlin("jvm")
    id("org.openapi.generator")
}

group = "com.somni"
version = "1.0.0"

val specPath = rootProject.file("backend/src/main/resources/openapi/somni-api.yaml")
val generatedDir = layout.buildDirectory.dir("generated/source/openapi").get().asFile

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(specPath.absolutePath)
    outputDir.set(generatedDir.absolutePath)
    skipValidateSpec.set(true)
    apiPackage.set("com.somni.api.client.api")
    modelPackage.set("com.somni.api.client.model")
    invokerPackage.set("com.somni.api.client.invoker")
    packageName.set("com.somni.api.client")
    configOptions.set(
        mapOf(
            "library" to "jvm-okhttp4",
            "dateLibrary" to "java8",
        ),
    )
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

sourceSets {
    main {
        kotlin.srcDir("$generatedDir/src/main/kotlin")
    }
}

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}
