// Dieses Modul stellt gemeinsame technische Hilfsfunktionen bereit,
// wie z.B. Konfigurations-Management, Datenbank-Verbindungen und Service Discovery.
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvmToolchain(21)

  // Target platforms
  jvm {
    compilerOptions {
      freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
  }

  js(IR) {
    browser {
      testTask {
        enabled = false
      }
    }
  }

  sourceSets {
    all {
      languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
    }

    commonMain.dependencies {
      // Domain models and types (core-utils depends on core-domain, not vice versa)
      api(projects.core.coreDomain)

      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.datetime)
      // Async support (available for all platforms)
      api(libs.kotlinx.coroutines.core)
      // Utilities (multiplatform compatible)
      api(libs.bignum)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }

    jvmMain.dependencies {
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
    jvmTest.dependencies {
      // Testing (JVM-specific)
      implementation(projects.platform.platformTesting)
      implementation(libs.bundles.testing.jvm)
      runtimeOnly(libs.postgresql.driver)
    }
  }
}

tasks.named<Test>("jvmTest") {
  useJUnitPlatform()
}
