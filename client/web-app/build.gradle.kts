plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                // Enable source maps for debugging
                devtool = "source-map"
            }
            // Configure webpack for production optimization
            webpackTask {
                mainOutputFileName = "web-app.js"
            }
            // Configure development server
            runTask {
                mainOutputFileName = "web-app.js"
                sourceMaps = true
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":client:common-ui"))
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(libs.ktor.client.js)
                implementation(libs.kotlinx.coroutines.core)
                // Add additional web-specific dependencies
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

compose.experimental {
    web.application {}
}

// Web-specific optimizations
tasks.named("jsBrowserDevelopmentWebpack") {
    outputs.upToDateWhen { false }
}

tasks.named("jsBrowserProductionWebpack") {
    outputs.upToDateWhen { false }
}
