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
      // UI Kit
      implementation(project(":clients:shared:common-ui"))

      // Shared Konfig & Utilities (AppConfig + BuildConfig)
      implementation(project(":clients:shared"))

      // Compose dependencies
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Ktor client for HTTP calls
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.serialization.kotlinx.json)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.client.auth)

      // Coroutines and serialization
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)

      // DateTime for multiplatform time handling
      implementation(libs.kotlinx.datetime)

      // ViewModel lifecycle
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)

    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
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
      implementation(libs.ktor.client.auth)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
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
      "-opt-in=kotlin.RequiresOptIn",
      // Suppress beta warning for expect/actual classes as per project decision
      "-Xexpect-actual-classes"
    )
  }
}
