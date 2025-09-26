@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

/**
 * Dieses Modul ist der "Host". Es kennt alle Features und die Shared-Module und
 * setzt sie zu einer lauffähigen Anwendung zusammen.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "at.mocode.clients"
version = "1.0.0"

kotlin {

    val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

    jvmToolchain(21)

    jvm {
        binaries {
            executable {
                mainClass.set("MainKt")
            }
        }
    }
    js(IR) {
        outputModuleName = "web-app"
        browser {
            commonWebpackConfig {
                cssSupport { enabled = true }
            }
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }

    if (enableWasm) {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            binaries.executable()
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {

        commonMain.dependencies {
            // Feature modules
            implementation(project(":clients:ping-feature"))

            // Shared modules
            implementation(project(":clients:shared:common-ui"))
            implementation(project(":clients:shared:navigation"))

            // Compose dependencies
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // ViewModel lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.coroutines.core)
        }

        jsMain.dependencies {
            implementation(npm("html-webpack-plugin", "5.6.4"))
        }

        if (enableWasm) {
            val wasmJsMain by getting {
                dependencies {
                    implementation(npm("html-webpack-plugin", "5.6.4"))
                }
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Configure duplicate handling strategy for distribution tasks
tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Ensure copy/sync-based distribution tasks exclude duplicates (e.g., index.html from resources and HtmlWebpackPlugin)
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.withType<Sync> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
