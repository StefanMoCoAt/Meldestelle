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
    jvm {
        binaries {
            executable {
                mainClass.set("MainKt")
            }
        }
    }
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }

    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
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
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
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
