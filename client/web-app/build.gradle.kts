plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "at.mocode.client.web"
version = "1.0.0"

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                devServer = devServer?.copy(
                    port = 8080,
                    static = mutableListOf("src/jsMain/resources")
                )

                // Webpack optimization settings
                configDirectory = project.projectDir.resolve("webpack.config.d")
            }
            webpackTask {
                args.add("--devtool=source-map")
            }
            runTask {
                args.add("--devtool=source-map")
            }

            // Add npm dependencies for webpack plugins
            useCommonJs()
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // Compose for Web
                implementation(compose.html.core)
                implementation(compose.runtime)

                // Common UI module (contains ViewModels and shared components)
                implementation(project(":client:common-ui"))

                // Coroutines for web
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")

            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
