/**
 * Dieses Modul definiert nur die Navigationsrouten.
 * Es ist noch simpler.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "at.mocode.clients.shared"
version = "1.0.0"

kotlin {
    jvm()
    js {
        browser()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                // No specific dependencies needed for navigation routes
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
