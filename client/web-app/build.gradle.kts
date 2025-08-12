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
            }
        }
    }
}

compose.experimental {
    web.application {}
}
