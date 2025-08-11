plugins {
    // KORREKTUR: Wir deklarieren dieses Modul als Kotlin Multiplatform Modul.
    alias(libs.plugins.kotlin.multiplatform)
    // KORREKTUR: Wir deklarieren, dass wir Jetpack Compose verwenden.
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    // Wir definieren die Zielplattformen, für die dieses Modul Code bereitstellt.
    jvm("desktop") // Ein JVM-Target für unsere Desktop-App
    js(IR) {       // Ein JavaScript-Target für unsere Web-App
        browser()
        binaries.executable()
    }

    // Hier definieren wir die Abhängigkeiten für die jeweiligen Source Sets.
    sourceSets {
        val commonMain by getting {
            dependencies {
                // --- Interne Module (für alle Plattformen verfügbar) ---
                api(projects.core.coreDomain)
                api(projects.core.coreUtils)

                // --- Jetpack Compose UI (für alle Plattformen verfügbar) ---
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                api(compose.components.resources)
                api(compose.materialIconsExtended)

                // --- Ktor Client für API-Kommunikation (Kernmodul für alle) ---
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)

                // --- Coroutines (für alle Plattformen verfügbar) ---
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val desktopMain by getting {
            dependencies {
                // Ktor-Engine, die nur für die Desktop (JVM) Version benötigt wird
                implementation(libs.ktor.client.cio)
            }
        }

        val jsMain by getting {
            dependencies {
                // Ktor-Engine, die nur für die Web (JS) Version benötigt wird
                implementation(libs.ktor.client.js)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
