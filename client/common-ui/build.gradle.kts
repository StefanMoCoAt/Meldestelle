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
    }
}
