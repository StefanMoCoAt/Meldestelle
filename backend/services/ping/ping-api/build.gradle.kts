plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode"
version = "1.0.0"

kotlin {
    // Toolchain is now handled centrally in the root build.gradle.kts

    val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

    // JVM target for backend usage
    jvm()

    // JS target for frontend usage (Compose/Browser)
    js {
        browser()
        // no need for binaries.executable() in a library
    }

    // Optional Wasm target for browser clients
    if (enableWasm) {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
