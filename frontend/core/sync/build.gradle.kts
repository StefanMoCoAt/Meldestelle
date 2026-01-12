plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  // Targets are configured centrally in the shells/feature modules; here we just provide common code.
  jvm()
  js(IR) {
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.coreDomain)

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
