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
  // Toolchain is now handled centrally in the root build.gradle.kts

  jvm()

  js {
    browser {
      testTask {
        // Browser testing is disabled to avoid environment issues (e.g. missing ChromeHeadless).
        // Tests are still run on JVM.
        enabled = false
      }
    }
  }

  // Wasm enabled by default
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      // Depend on core domain for User/Role types used by navigation API
      implementation(project(":frontend:core:domain"))
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
