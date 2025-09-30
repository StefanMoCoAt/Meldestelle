@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
                // Webpack-Mode abhängig von Build-Typ
                mode = if (project.hasProperty("production"))
                    KotlinWebpackConfig.Mode.PRODUCTION
                else
                    KotlinWebpackConfig.Mode.DEVELOPMENT
            }
            webpackTask {
                mainOutputFileName = "web-app.js"
                output.libraryTarget = "commonjs2"
            }
            // Development Server konfigurieren
            runTask {
                mainOutputFileName.set("web-app.js")
            }
            // Browser-Tests komplett deaktivieren (Configuration Cache kompatibel)
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
            wasmJsMain.dependencies {
                implementation(npm("html-webpack-plugin", "5.6.4"))
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xskip-metadata-version-check" // Für bleeding-edge Versionen
        )
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
    duplicatesStrategy = DuplicatesStrategy.WARN // Statt EXCLUDE
}

tasks.withType<Sync> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
