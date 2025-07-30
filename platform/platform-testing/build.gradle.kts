plugins {
    // KORREKTUR: 'java-library' und 'kotlin("jvm")' ersetzen
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    // KORREKTUR: JVM- und JS-Ziele definieren
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        // Diese Abhängigkeiten sind für alle Plattformen (JVM, JS) verfügbar
        val commonTest by getting {
            dependencies {
                // Die 'kotlin("test")'-Abhängigkeit ist der Standardweg für KMP-Tests
                implementation(kotlin("test"))
                api(libs.kotlinx.coroutines.test)
            }
        }

        // Diese Abhängigkeiten sind NUR für die JVM-Tests verfügbar
        val jvmTest by getting {
            dependencies {
                api(libs.junit.jupiter.api)
                api(libs.junit.jupiter.engine)
                api(libs.junit.jupiter.params)
                api(libs.junit.platform.launcher)

                // KORREKTUR: Alle hartcodierten Versionen durch Aliase ersetzen
                api(libs.mockk)
                api(libs.assertj.core)
                api(libs.spring.boot.starter.test)
                api(libs.h2.driver)

                api(libs.testcontainers.core)
                api(libs.testcontainers.junit.jupiter)
                api(libs.testcontainers.postgresql)
            }
        }
    }
}
