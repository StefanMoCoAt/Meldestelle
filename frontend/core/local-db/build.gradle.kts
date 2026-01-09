@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts
  val enableWasm = providers.gradleProperty("enableWasm").orNull == "true"

  jvm()
  js {
    browser {
      testTask { enabled = false }
    }
  }

  if (enableWasm) {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
      browser()
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.koin.core)
      implementation(libs.bundles.kmp.common) // Coroutines, Serialization, DateTime
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.coroutines)
    }

    jvmMain.dependencies {
      implementation(libs.sqldelight.driver.sqlite)
    }

    jsMain.dependencies {
      implementation(libs.sqldelight.driver.web)
    }

    if (enableWasm) {
      val wasmJsMain = getByName("wasmJsMain")
      wasmJsMain.dependencies {
        implementation(libs.sqldelight.driver.web)
      }
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
      generateAsync.set(true) // WICHTIG: Async-First für JS/Wasm Support
    }
  }
}
