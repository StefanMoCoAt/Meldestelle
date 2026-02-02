plugins {
  // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvm()
  js(IR) {
    binaries.library()
    // Re-enabled browser environment after Root NodeJs fix
    browser {
        testTask {
            enabled = false
        }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
    }
  }
}
