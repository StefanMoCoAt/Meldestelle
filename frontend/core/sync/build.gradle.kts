plugins {
  alias(libs.plugins.kotlinMultiplatform)
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
      // Correct dependency: Syncable interface is in shared core domain
      implementation(projects.core.coreDomain)
      // Also include frontend domain if needed (e.g. for frontend specific models)
      implementation(projects.frontend.core.domain)

      // Networking
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.serialization.kotlinx.json)

      // Serialization
      implementation(libs.kotlinx.serialization.json)

      // DI
      implementation(libs.koin.core)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
