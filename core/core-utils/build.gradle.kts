// Dieses Modul stellt gemeinsame technische Hilfsfunktionen bereit,
// wie z.B. Konfigurations-Management, Datenbank-Verbindungen und Service Discovery.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    // Target platforms
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
                // Abhängigkeit zum core-domain-Modul, um dessen Typen zu verwenden
                api(projects.core.coreDomain)

                // Asynchronität (available for all platforms) - explicit version to avoid BOM issues
                api(libs.kotlinx.coroutines.core)


                // Utilities (multiplatform compatible)
                api(libs.bignum)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Abhängigkeit zum platform-Modul für zentrale Versionsverwaltung
                api(projects.platform.platformDependencies)

                // Datenbank-Management (JVM-specific)
                // OPTIMIERUNG: Verwendung von Bundles für Exposed und Flyway
                api(libs.bundles.exposed)
                api(libs.bundles.flyway)
                api(libs.hikari.cp)

                // Service Discovery (JVM-specific)
                // api(libs.consul.client) wird getauscht mir spring-cloud-starter-consul-discovery
                api(libs.spring.cloud.starter.consul.discovery)

                // Logging (JVM-specific)
                api(libs.kotlin.logging.jvm)

                // JVM-specific utilities
                implementation(libs.room.common.jvm) // Für BigDecimal Serialisierung
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                // Testing (JVM-specific)
                implementation(projects.platform.platformTesting)
                implementation(libs.bundles.testing.jvm)
                runtimeOnly(libs.postgresql.driver)
            }
        }
    }
}
