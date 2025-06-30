@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat // HIER FEHLTE DER IMPORT
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")

    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val commonMain by getting {
            dependencies {
                commonMain.dependencies {
                    implementation(projects.shared)
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(compose.components.uiToolingPreview)
                }
                desktopMain.dependencies {
                    implementation(compose.desktop.currentOs)
                    implementation(libs.kotlinx.coroutines.swing)
                }
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "at.mocode.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "at.mocode"
            packageVersion = "1.0.0"
        }
    }
}
