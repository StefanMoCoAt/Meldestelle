// Core domain objects of the Shared kernel
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core dependencies (that aren't included in platform-dependencies)
                api(libs.uuid)
                // Serialization and date-time for commonMain
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jsMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Fachliches Domain-Modul: keine technischen Abhängigkeiten hier hinterlegen.
                // Falls in Zukunft JVM-spezifische, fachlich neutrale Ergänzungen nötig sind,
                // bitte bewusst und minimal hinzufügen.
            }
        }

        val jvmTest by getting {
            dependencies {
//                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter.api)
                implementation(libs.mockk)
                implementation(projects.platform.platformTesting)
                implementation(libs.bundles.testing.jvm)
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
