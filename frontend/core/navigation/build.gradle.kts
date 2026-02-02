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
  jvm()
  js {
    binaries.library()
    browser {
        testTask { enabled = false }
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
