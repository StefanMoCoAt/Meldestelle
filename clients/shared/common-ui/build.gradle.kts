/**
 * Dieses Modul stellt "dumme", wiederverwendbare UI-Komponenten und das Theme bereit.
 * Es darf keine Ahnung von irgendeiner Fachlichkeit haben.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "at.mocode.clients.shared"
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
