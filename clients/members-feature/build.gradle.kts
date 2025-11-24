import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
            testTask { enabled = false }
        }
    }

    if (enableWasm) {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs { browser() }
    }

    sourceSets {
        commonMain.dependencies {
            // UI Kit
            implementation(project(":clients:shared:common-ui"))
            // Shared config/utilities
            implementation(project(":clients:shared"))
            // Authentication helpers (token + authenticated client)
            implementation(project(":clients:auth-feature"))

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            // ViewModel lifecycle + compose helpers
            implementation(libs.bundles.compose.common)

            // HTTP + Kotlinx
            implementation(libs.bundles.ktor.client.common)
            implementation(libs.bundles.kotlinx.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        if (enableWasm) {
            val wasmJsMain = getByName("wasmJsMain")
            wasmJsMain.dependencies {
                implementation(libs.ktor.client.js)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}
