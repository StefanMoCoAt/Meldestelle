/**
 * Dieses Modul definiert nur die Navigationsrouten.
 * Es ist noch simpler.
 */
plugins {
  // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
  id("org.jetbrains.kotlin.multiplatform")
}

group = "at.mocode.clients.shared"
version = "1.0.0"

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
      // Depend on core domain for User/Role types used by navigation API
      implementation(project(":frontend:core:domain"))
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
