plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Toolchain is now handled centrally in the root build.gradle.kts

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

  // Wasm support enabled?
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
      browser()
  }

  sourceSets {
    // Opt-in to experimental Kotlin UUID API across all source sets
    all {
      languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
      // Opt-in für kotlin.time.ExperimentalTime projektweit, solange Teile noch experimentell sind
      languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    commonMain.dependencies {
      // Core dependencies (that aren't included in platform-dependencies)
      // Note: core-domain should NOT depend on core-utils to avoid circular dependencies
      // core-utils depends on core-domain, not the other way around

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
      // implementation(libs.bundles.testing.jvm) // Temporarily disabled due to resolution issues
      implementation(libs.junit.jupiter.api)
      implementation(libs.junit.jupiter.engine)
      implementation(libs.junit.jupiter.params)
      implementation(libs.junit.platform.launcher)
      implementation(libs.mockk)
      implementation(libs.assertj.core)
      implementation(libs.kotlinx.coroutines.test)
    }

  }
}

tasks.named<Test>("jvmTest") {
  useJUnitPlatform()
}
