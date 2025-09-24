plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode"
version = "1.0.0"

kotlin {
    // JVM target for backend usage
    jvm()

    // JS target for frontend usage (Compose/Browser)
    js {
        browser()
        // no need for binaries.executable() in a library
    }

    // Align toolchain with project (see composeApp uses 21)
    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
