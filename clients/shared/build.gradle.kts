@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Shared Module: Gemeinsame Libraries und Utilities für alle Client-Features
 * KEINE EXECUTABLE - ist eine Library für andere Module
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

    jvmToolchain(21)

    // JVM Target für Desktop
    jvm()

    // JavaScript Target für Web
    js(IR) {
        browser()
    }

    // WASM, nur wenn explizit aktiviert
    if (enableWasm) {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs { browser() }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines für asynchrone Programmierung
            implementation(libs.kotlinx.coroutines.core)

            // Serialization für JSON
            implementation(libs.kotlinx.serialization.json)

            // HTTP Client
//            implementation(libs.ktor.client.core)
//            implementation(libs.ktor.client.contentNegotiation)
//            implementation(libs.ktor.client.serialization.kotlinx.json)
//            implementation(libs.ktor.client.logging)
//            implementation(libs.ktor.client.auth)

            // DateTime
            implementation(libs.kotlinx.datetime)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)

            // Compose für shared UI components
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)

            // Compose für shared UI components
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }

        // WASM SourceSet, nur wenn aktiviert
        if (enableWasm) {
            val wasmJsMain = getByName("wasmJsMain")
            wasmJsMain.dependencies {
                implementation(libs.ktor.client.js) // WASM verwendet JS-Client [cite: 7]

                // ✅ HINZUFÜGEN: Compose für shared UI components für WASM
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
    }
}

// KMP Compile-Optionen
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}
