/**
 * Dieses Modul kapselt die gesamte UI und Logik für das Authentication-Feature.
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
    val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

    jvmToolchain(21)

    jvm()

    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    if (enableWasm) {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }
    }

    sourceSets {
        commonMain.dependencies {
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
            // DateTime for multiplatform time handling
            implementation(libs.kotlinx.datetime)
            // ViewModel lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
        }

        jvmTest.dependencies {
            implementation(libs.mockk)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
