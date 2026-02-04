plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode.frontend.core"
version = "1.0.0"

kotlin {
  jvm()
  js {
    binaries.library()
    browser {
      testTask {
        enabled = false
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      // UI Kit (Design System)
      implementation(projects.frontend.core.designSystem)

      // Network core (provides apiClient + TokenProvider interface)
      implementation(projects.frontend.core.network)

      // Domain core (provides AppConstants)
      implementation(projects.frontend.core.domain)

      // Compose dependencies
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Bundles
      implementation(libs.bundles.kmp.common)
      implementation(libs.bundles.ktor.client.common)
      implementation(libs.bundles.compose.common)

      // DI
      implementation(libs.koin.core)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
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
    }
  }
}
