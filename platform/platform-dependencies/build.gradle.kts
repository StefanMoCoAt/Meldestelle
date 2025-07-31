/*
// Multiplatform
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.kotlin.logging.jvm)
                api(libs.kotlinx.coroutines.reactor)
            }
        }
    }
}
*/

// Dieses Modul ist ein reines "Sammelmodul".
// Es hat keinen eigenen Code, sondern bündelt nur gemeinsame Laufzeit-Abhängigkeiten,
// die von den meisten JVM-Modulen benötigt werden.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Importiert die zentrale BOM, um konsistente Versionen zu gewährleisten.
    api(platform(projects.platform.platformBom))

    // Stellt die wichtigsten Kotlin(x)-Bibliotheken via `api` bereit,
    // damit jedes Modul, das von `platform-dependencies` abhängt, diese automatisch erhält.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    api(libs.kotlin.logging.jvm)
    api(libs.kotlinx.coroutines.reactor)
}
