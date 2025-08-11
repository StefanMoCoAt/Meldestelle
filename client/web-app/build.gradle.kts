plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser {
            // Konfiguriert den Development-Server und die finalen Bundles.
            commonWebpackConfig {
                outputFileName = "MeldestelleWebApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // Greift explizit auf den JS-Teil unseres KMP-Moduls zu.
                implementation(projects.client.commonUi)

                // Stellt die Web-spezifischen (HTML) Teile von Jetpack Compose bereit.
                implementation(compose.html.core)

                // HTTP client for making requests to the backend
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
