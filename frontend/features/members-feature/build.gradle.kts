import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Dieses Modul kapselt die gesamte UI und Logik für das Authentication-Feature.
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
      // UI Kit (Design System)
      implementation(project(":frontend:core:design-system"))

      // Shared Konfig & Utilities (AppConfig + BuildConfig)
      implementation(project(":frontend:shared"))

      // Compose dependencies
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Bundles (Cleaned up dependencies)
      implementation(libs.bundles.kmp.common)        // Coroutines, Serialization, DateTime
      implementation(libs.bundles.ktor.client.common) // Ktor Client (Core, Auth, JSON, Logging)
      implementation(libs.bundles.compose.common)     // ViewModel & Lifecycle
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
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }

    // WASM SourceSet, nur wenn aktiviert
    if (enableWasm) {
      val wasmJsMain = getByName("wasmJsMain")
      wasmJsMain.dependencies {
        implementation(libs.ktor.client.js) // WASM verwendet JS-Client [cite: 7]

        // Compose für shared UI components für WASM
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
    jvmTarget.set(JvmTarget.JVM_25)
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn",
      // Suppress beta warning for expect/actual classes as per project decision
      "-Xexpect-actual-classes"
    )
  }
}
