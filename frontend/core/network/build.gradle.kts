@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvmToolchain(21)

  jvm()
  js {
    browser {
      testTask { enabled = false }
    }
    binaries.executable()
  }

  if (enableWasm) {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }
  }

  sourceSets {
    commonMain.dependencies {
      // Ktor Client core + JSON + Auth + Logging + Timeouts + Retry
      api(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.serialization.kotlinx.json)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.logging)
      // ktor-client-resources optional; disabled until version is added to catalog

      // Kotlinx core bundles
      implementation(libs.bundles.kotlinx.core)

      // DI (Koin)
      api(libs.koin.core)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    jsMain.dependencies {
      implementation(libs.ktor.client.js)
    }

    if (enableWasm) {
      val wasmJsMain = getByName("wasmJsMain")
      wasmJsMain.dependencies {
        implementation(libs.ktor.client.js)
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
  }
}
