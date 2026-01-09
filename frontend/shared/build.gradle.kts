plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    js(IR) {
        browser()
        binaries.executable()
    }

    // Wasm vorerst deaktiviert
    /*
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    */

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.frontend.core.domain)
                // implementation(projects.frontend.core.designSystem) // REMOVED: Circular dependency
                implementation(projects.frontend.core.navigation)
                implementation(projects.frontend.core.network)
                implementation(projects.frontend.core.localDb)

                // Features - REMOVED: Circular dependency. Shared should NOT depend on features.
                // implementation(projects.frontend.features.authFeature)
                // implementation(projects.frontend.features.pingFeature)

                // KMP Bundles
                implementation(libs.bundles.kmp.common)
                implementation(libs.bundles.compose.common)

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        /*
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        */
    }
}
