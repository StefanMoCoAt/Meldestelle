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
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvmToolchain(21)

  jvm()

  js {
    browser {
      testTask {
        enabled = false
      }
    }
    binaries.executable()
  }

  // WASM, nur wenn explizit aktiviert
  if (enableWasm) {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
      browser()
      binaries.executable()
    }
  }

  sourceSets {
    commonMain.dependencies {
      // Contract from backend
      implementation(projects.services.ping.pingApi)

      // UI Kit
      implementation(project(":clients:shared:common-ui"))

      // Shared Konfig & Utilities
      implementation(project(":clients:shared"))

      // Compose dependencies
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Ktor client for HTTP calls
      implementation(libs.bundles.ktor.client.common)

      // Coroutines and serialization
      implementation(libs.bundles.kotlinx.core)

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
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
      }
    }
  }
}

// KMP Compile-Optionen
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn"
    )
  }
}
