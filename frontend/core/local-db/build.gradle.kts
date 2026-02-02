@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  // Fix for "Plugin loaded multiple times": Apply plugin by ID without version (inherited from root)
  id("org.jetbrains.kotlin.multiplatform")
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
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
      implementation(libs.koin.core)
      implementation(libs.bundles.kmp.common)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.coroutines)
    }

    jvmMain.dependencies {
      implementation(libs.sqldelight.driver.sqlite)
    }

    jsMain.dependencies {
      implementation(libs.sqldelight.driver.web)
      implementation(npm("@sqlite.org/sqlite-wasm", "3.51.1-build2"))
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

sqldelight {
  databases {
    create("AppDatabase") {
      packageName.set("at.mocode.frontend.core.localdb")
      generateAsync.set(true)
    }
  }
}
