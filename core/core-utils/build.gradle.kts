plugins {
    // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    js {
        browser()
    }
    // Wasm support enabled?
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.coreDomain)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmMain {
            dependencies {
                // Removed Exposed dependencies to make this module KMP compatible
                // implementation(libs.exposed.core)
                // implementation(libs.exposed.jdbc)
            }
        }
    }
}
