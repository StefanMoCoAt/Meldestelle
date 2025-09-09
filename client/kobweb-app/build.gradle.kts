plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

group = "at.mocode.client.kobweb"
version = "1.0-SNAPSHOT"

kotlin {
    js(IR) {
        outputModuleName.set("kobweb-app")
        browser {
            commonWebpackConfig {
                outputFileName = "kobweb-app.js"
            }
        }
        binaries.executable()
    }

    @Suppress("UNUSED_VARIABLE") // Suppress spurious warnings about the outputs not being used anywhere

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }

        val jsMain by getting {
            dependencies {
                // Kobweb dependencies
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk.core)
                implementation(libs.kobwebx.markdown)

                // Compose HTML (CSS, DOM)
                implementation(libs.compose.html.core)

                // Common UI module (preserving business logic)
                implementation(project(":client:common-ui"))

                // Additional web-specific dependencies
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.js)
            }
        }

    }
}
