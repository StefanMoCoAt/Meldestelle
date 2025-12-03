plugins {
    // KORREKTUR: Von 'kotlin("jvm")' zu Multiplattform wechseln.
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Hier die jeweiligen Modul-Abhängigkeiten eintragen
                // z.B. für events-domain:
                implementation(projects.core.coreDomain)

                // z.B. für events-application:
                // implementation(projects.events.eventsDomain)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(projects.platform.platformTesting)
            }
        }
    }
}
