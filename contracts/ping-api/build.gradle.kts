plugins {
    // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode"
version = "1.0.0"

kotlin {
    // Toolchain is now handled centrally in the root build.gradle.kts

    // JVM target for backend usage
    jvm()

    // JS target for frontend usage (Compose/Browser)
    js {
        browser()
        // no need for binaries.executable() in a library
    }

    // Wasm enabled by default
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.coreDomain) // Changed from implementation to api to export Syncable
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
