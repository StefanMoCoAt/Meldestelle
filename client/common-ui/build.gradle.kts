plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm()
    js(IR) { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                // Skiko is provided by Compose Multiplatform - no explicit dependency needed

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // Ktor Client for API calls
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)

                // Coroutines for background tasks
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                // Ktor engine for Desktop
                implementation(libs.ktor.client.cio)
            }
        }
        val jsMain by getting {
            dependencies {
                // Ktor engine for Browser
                implementation(libs.ktor.client.js)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                // Note: ktor-client-mock would be ideal but may not be available in libs
                // Using core testing dependencies for now
            }
        }
    }
}
