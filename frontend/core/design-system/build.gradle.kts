plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvm()
  js(IR) {
    browser()
//        nodejs()
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
      // Shared module dependency
      implementation(projects.frontend.shared)

      // Compose dependencies
      implementation("org.jetbrains.compose.runtime:runtime:1.10.0-rc02")
      implementation("org.jetbrains.compose.foundation:foundation:1.10.0-rc02")
      implementation("org.jetbrains.compose.material3:material3:1.9.0-beta03")
      implementation("org.jetbrains.compose.ui:ui:1.10.0-rc02")
      implementation("org.jetbrains.compose.components:components-resources:1.10.0-rc02")

      // Coroutines
      implementation(libs.kotlinx.coroutines.core)

      // Serialization
      implementation(libs.kotlinx.serialization.json)

      // DateTime
      implementation(libs.kotlinx.datetime)
    }

    jsMain.dependencies {
      // JS-specific UI dependencies if needed
    }

    jvmMain.dependencies {
      // JVM-specific UI dependencies if needed
    }
  }
}
