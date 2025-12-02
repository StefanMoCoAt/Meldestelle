/**
 * Dieses Modul definiert nur die Navigationsrouten.
 * Es ist noch simpler.
 */
plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

group = "at.mocode.clients.shared"
version = "1.0.0"

kotlin {
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvmToolchain(21)

  jvm()

  js {
    browser()
  }

  if (enableWasm) {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
      browser()
    }
  }

  sourceSets {
    commonMain.dependencies {
      // No specific dependencies needed for navigation routes
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
