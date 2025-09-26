plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(21)

    jvm {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Core dependencies (that aren't included in platform-dependencies)
            api(libs.uuid)
            // Serialization and date-time for commonMain
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jsMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }

        jsTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            // Fachliches Domain-Modul: keine technischen Abhängigkeiten hier hinterlegen.
            // Falls in Zukunft JVM-spezifische, fachlich neutrale Ergänzungen nötig sind,
            // bitte bewusst und minimal hinzufügen.
        }

        jvmTest.dependencies {
            // implementation(kotlin("test-junit5"))
            implementation(libs.junit.jupiter.api)
            implementation(libs.mockk)
            implementation(projects.platform.platformTesting)
            implementation(libs.bundles.testing.jvm)
        }

    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
