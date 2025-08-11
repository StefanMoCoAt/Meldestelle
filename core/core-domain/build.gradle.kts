// Dieses Modul definiert die Kern-Domänenobjekte des Shared Kernels.
// Es enthält keine Implementierungsdetails, nur reine Datenklassen und Enums.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // Target platforms
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xopt-in=kotlin.time.ExperimentalTime")
        }
    }
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kern-Abhängigkeiten für das Domänen-Modul (common for all platforms)
                api(libs.uuid)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Stellt sicher, dass dieses Modul Zugriff auf die im zentralen Katalog
                // definierten Bibliotheken hat (JVM-specific)
                api(projects.platform.platformDependencies)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                // Stellt die Test-Bibliotheken bereit (JVM-specific)
                implementation(projects.platform.platformTesting)
                implementation(libs.bundles.testing.jvm)
            }
        }
    }
}
