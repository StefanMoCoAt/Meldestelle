plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvm()
  js {
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
      implementation(libs.kotlinx.serialization.json)
    }
  }
}

// KMP Compile-Optionen sind jetzt zentral in der Root build.gradle.kts definiert
