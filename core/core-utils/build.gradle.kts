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
                // Dependency on core-domain module to use its types
                api(projects.core.coreDomain)

                // Async support (available for all platforms)
                api(libs.kotlinx.coroutines.core)

                // Utilities (multiplatform compatible)
                api(libs.bignum)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                // JVM-specific dependencies - access to central catalog
                api(projects.platform.platformDependencies)

                // Database Management (JVM-specific)
                api(libs.bundles.exposed)
                api(libs.bundles.flyway)
                api(libs.hikari.cp)

                // Service Discovery (JVM-specific)
                api(libs.spring.cloud.starter.consul.discovery)

                // Logging (JVM-specific)
                api(libs.kotlin.logging.jvm)

                // Jakarta Annotation API
                api(libs.jakarta.annotation.api)

                // JSON Processing
                api(libs.jackson.module.kotlin)
                api(libs.jackson.datatype.jsr310)
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

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
