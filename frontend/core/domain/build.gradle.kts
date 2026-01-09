@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts

  // Wasm is now a first-class citizen in our stack, so we enable it by default
  // val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvm()
  js {
    browser {
      testTask { enabled = false }
    }
  }

  // Always enable Wasm to match the rest of the KMP stack
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs { browser() }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.json)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
  }
}
