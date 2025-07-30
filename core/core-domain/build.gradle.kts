plugins {
    // KORREKTUR: Von JVM zu Multiplattform wechseln
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

// KORREKTUR: Diesen Block hinzufügen, um die Ziele zu definieren
kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Die Abhängigkeiten kommen hier rein
                api(projects.platform.platformDependencies)
                api(libs.uuid)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(projects.platform.platformTesting)
            }
        }
    }
}
