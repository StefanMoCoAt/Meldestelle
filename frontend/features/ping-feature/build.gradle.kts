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
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvm()

  js {
    browser {
      testTask {
        enabled = false
      }
    }
  }

  // WASM, nur wenn explizit aktiviert
  if (enableWasm) {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
      browser()
    }
  }

  sourceSets {
    commonMain.dependencies {
      // Contract from backend
      implementation(projects.backend.services.ping.pingApi)

      // UI Kit (Design System)
      implementation(projects.frontend.core.designSystem)

      // Shared Konfig & Utilities
      implementation(projects.frontend.shared)

      // Compose dependencies
      implementation("org.jetbrains.compose.foundation:foundation:1.10.0-rc02")
      implementation("org.jetbrains.compose.runtime:runtime:1.10.0-rc02")
      implementation("org.jetbrains.compose.material3:material3:1.9.0-beta03")
      implementation("org.jetbrains.compose.ui:ui:1.10.0-rc02")
      implementation("org.jetbrains.compose.components:components-resources:1.10.0-rc02")
      implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

      // Ktor client for HTTP calls
      implementation(libs.ktor.client.core)

      // Coroutines and serialization
      implementation(libs.kotlinx.coroutines.core)

      // DI (Koin) for resolving apiClient from container
      implementation(libs.koin.core)

      // ViewModel lifecycle
      implementation(libs.bundles.compose.common)

    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)

    }

    jvmTest.dependencies {
      implementation(libs.mockk)
      implementation(projects.platform.platformTesting)
      implementation(libs.bundles.testing.jvm)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
      // Auth-Models Zugriff (nur für JVM)
      //implementation(project(":infrastructure:auth:auth-client"))
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)

    }

    // WASM SourceSet, nur wenn aktiviert
    if (enableWasm) {
      val wasmJsMain = getByName("wasmJsMain")
      wasmJsMain.dependencies {
        implementation(libs.ktor.client.js) // WASM verwendet JS-Client [cite: 7]

        // ✅ HINZUFÜGEN: Compose für shared UI components für WASM
        implementation("org.jetbrains.compose.runtime:runtime:1.10.0-rc02")
        implementation("org.jetbrains.compose.foundation:foundation:1.10.0-rc02")
        implementation("org.jetbrains.compose.material3:material3:1.9.0-beta03")

      }
    }
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
