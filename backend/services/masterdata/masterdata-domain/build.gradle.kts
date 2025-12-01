plugins {
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
                // KORREKTUR: Diese zwei Zeilen hinzufügen
                implementation(projects.core.coreDomain)
                implementation(projects.core.coreUtils)
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
