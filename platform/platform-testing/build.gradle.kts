/*
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                api(libs.kotlinx.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                api(libs.junit.jupiter.api)
                api(libs.junit.jupiter.engine)
                api(libs.junit.jupiter.params)
                api(libs.junit.platform.launcher)

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

 */

// Dieses Modul bündelt alle für JVM-Tests notwendigen Abhängigkeiten.
// Jedes Modul, das Tests enthält, sollte dieses Modul mit `testImplementation` einbinden.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Importiert die zentrale BOM für konsistente Versionen.
    api(platform(projects.platform.platformBom))

    // Diese Bundles sind in `libs.versions.toml` definiert.
    api(libs.bundles.testing.jvm)
    api(libs.bundles.testcontainers)

    // Stellt Spring Boot Test-Abhängigkeiten und die H2-Datenbank für Tests bereit.
    api(libs.spring.boot.starter.test)
    api(libs.h2.driver)
}
