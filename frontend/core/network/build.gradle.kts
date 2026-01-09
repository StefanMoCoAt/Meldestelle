@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts

  jvm()
  js {
    browser {
      testTask { enabled = false }
    }
  }

  // Wasm enabled by default
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs { browser() }

  sourceSets {
    commonMain.dependencies {
      // Ktor Client core + JSON and Auth + Logging + Timeouts + Retry
      api(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.serialization.kotlinx.json)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.logging)
      // ktor-client-resources optional; disabled until version is added to catalog

      // Kotlinx core bundles
      implementation(libs.kotlinx.coroutines.core)

      // DI (Koin)
      api(libs.koin.core)

      // Project modules via typesafe accessors
      // (none here; kept for consistency)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }

    val wasmJsMain = getByName("wasmJsMain")
    wasmJsMain.dependencies {
      implementation(libs.ktor.client.js)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
  }
}
