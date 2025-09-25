/**
 * Dieses Modul stellt "dumme", wiederverwendbare UI-Komponenten und das Theme bereit.
 * Es darf keine Ahnung von irgendeiner Fachlichkeit haben.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "at.mocode.clients.shared"
version = "1.0.0"

kotlin {
    jvm()
    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
