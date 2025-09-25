/**
 * Dieses Modul kapselt die gesamte UI und Logik für das Ping-Feature.
 * Es kennt seine eigenen technischen Abhängigkeiten (Ktor, Coroutines)
 * und den UI-Baukasten (common-ui), aber es kennt keine anderen Features.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode.clients"
version = "1.0.0"

kotlin {
    jvm()
    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Contract from backend
                implementation(projects.services.ping.pingApi)

                // UI Kit
                implementation(project(":clients:shared:common-ui"))

                // Compose dependencies
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                // Ktor client for HTTP calls
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)

                // Coroutines and serialization
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // ViewModel lifecycle
                implementation(libs.androidx.lifecycle.viewmodelCompose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
    }
}
