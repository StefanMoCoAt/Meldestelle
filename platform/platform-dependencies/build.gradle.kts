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
                // KORREKTUR: Die explizite `platform()`-Abhängigkeit wird hier entfernt.
                // Die Versionen aus der BOM werden trotzdem angewendet.

                // KORREKTUR: `stdlib` und `reflect` werden entfernt.
                // `stdlib` wird automatisch hinzugefügt.

                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

                // KORREKTUR: Hartcodierte Version durch Alias ersetzen
                api(libs.kotlin.logging.jvm)
            }
        }
    }
}
