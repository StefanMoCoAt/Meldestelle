plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode"
version = "1.0.0"

kotlin {
  // JVM target for backend usage
  jvm()

  // JS target for frontend usage (Compose/Browser)
  js {
    browser()
  }

  // Wasm enabled by default
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.coreDomain) // Changed from implementation to api to export Syncable
        implementation(libs.kotlinx.serialization.json)
      }
    }
    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }
  }
}
