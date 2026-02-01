import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Dieses Modul kapselt die gesamte UI und Logik für das Ping-Feature.
 * Es kennt seine eigenen technischen Abhängigkeiten (Ktor, Coroutines)
 * und den UI-Baukasten (common-ui), aber es kennt keine anderen Features.
 */
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode.clients"
version = "1.0.0"

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts

  jvm()

  js {
    browser {
      testTask {
        enabled = false
      }
    }
  }

  // Wasm vorerst deaktiviert
  /*
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
  */

  sourceSets {
    commonMain.dependencies {
      // Contract from backend
      implementation(projects.contracts.pingApi)

      // UI Kit (Design System)
      implementation(projects.frontend.core.designSystem)

      // Generic Delta-Sync core
      implementation(projects.frontend.core.sync)

      // Local DB (SQLDelight)
      implementation(projects.frontend.core.localDb)
      implementation(libs.sqldelight.coroutines) // Explicitly add coroutines extension for async driver support

      // Shared sync contract base (Syncable)
      implementation(projects.core.coreDomain)

      // Compose dependencies
      implementation(compose.foundation)
      implementation(compose.runtime)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Bundles (Cleaned up dependencies)
      implementation(libs.bundles.kmp.common)        // Coroutines, Serialization, DateTime
      implementation(libs.bundles.ktor.client.common) // Ktor Client (Core, Auth, JSON, Logging)
      implementation(libs.bundles.compose.common)     // ViewModel & Lifecycle

      // DI (Koin) for resolving apiClient from container
      implementation(libs.koin.core)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    jvmTest.dependencies {
      implementation(libs.mockk) // MockK only for JVM tests
      implementation(projects.platform.platformTesting)
      implementation(libs.bundles.testing.jvm)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }

    /*
    val wasmJsMain = getByName("wasmJsMain")
    wasmJsMain.dependencies {
      implementation(libs.ktor.client.js) // WASM verwendet JS-Client [cite: 7]

      // Compose für shared UI components für WASM
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
    }
    */
  }
}

// KMP Compile-Optionen
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn"
    )
  }
}
