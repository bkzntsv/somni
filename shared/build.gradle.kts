plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

kotlin {
    // Android target is optional - uncomment when Android support is needed
    // Requires ANDROID_HOME environment variable or local.properties with sdk.dir
    // androidTarget {
    //     compilations.all {
    //         kotlinOptions {
    //             jvmTarget = "17"
    //         }
    //     }
    // }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(Dependencies.koinCore)
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Dependencies.kotlinStdlib)
                implementation(Dependencies.coroutinesCore)
                implementation(Dependencies.serializationJson)
                implementation(Dependencies.datetime)
                implementation(Dependencies.ktorClientCore)
                implementation(Dependencies.ktorClientContentNegotiation)
                implementation(Dependencies.ktorClientLogging)
                implementation(Dependencies.ktorSerializationJson)
                api(Dependencies.koinCore)
                implementation(Dependencies.sqlDelightRuntime)
                implementation(Dependencies.sqlDelightCoroutinesExt)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(Dependencies.kotlinTest)
                implementation(Dependencies.coroutinesTest)
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            
            dependencies {
                implementation(Dependencies.ktorClientDarwin)
                implementation(Dependencies.sqlDelightNativeDriver)
            }
        }
        
        // Android source set - uncomment when Android support is enabled
        // val androidMain by getting {
        //     dependencies {
        //         implementation(Dependencies.ktorClientAndroid)
        //         implementation(Dependencies.sqlDelightAndroidDriver)
        //         implementation(Dependencies.sqlCipherAndroid)
        //     }
        // }
    }
}

// Android configuration - uncomment when Android support is enabled
// android {
//     namespace = "com.somni.shared"
//     compileSdk = 34
//     
//     defaultConfig {
//         minSdk = 26
//     }
//     
//     compileOptions {
//         sourceCompatibility = JavaVersion.VERSION_17
//         targetCompatibility = JavaVersion.VERSION_17
//     }
// }

sqldelight {
    databases {
        create("SomniDatabase") {
            packageName.set("com.somni.database")
            generateAsync.set(true)
        }
    }
}
