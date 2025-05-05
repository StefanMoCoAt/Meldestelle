import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // put your Multiplatform dependencies here
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("com.benasher44:uuid:0.8.1")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val wasmJsMain by getting {
            dependsOn(commonMain)
        }
    }
}
