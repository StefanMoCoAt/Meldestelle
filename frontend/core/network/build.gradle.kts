@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
  id("org.jetbrains.kotlin.multiplatform")
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
      api(libs.ktor.client.core)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.serialization.kotlinx.json)
      implementation(libs.ktor.client.auth)
      implementation(libs.ktor.client.logging)
      implementation(libs.kotlinx.coroutines.core)
      api(libs.koin.core)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.cio)
    }

    jsMain.dependencies {
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
