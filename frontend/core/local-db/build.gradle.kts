@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  // Toolchain is now handled centrally in the root build.gradle.kts

  jvm()
  js {
    browser {
      testTask { enabled = false }
    }
    binaries.executable()
  }

  // Wasm vorerst deaktiviert, um Stabilität mit JS zu gewährleisten
  /*
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
  */

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

      // NPM deps used by `sqlite.worker.js` (OPFS-backed SQLite WASM worker)
      implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
      // Use a published build tag from the official package.
      implementation(npm("@sqlite.org/sqlite-wasm", "3.51.1-build2"))
    }

    /*
    val wasmJsMain = getByName("wasmJsMain")
    wasmJsMain.dependencies {
      implementation(libs.sqldelight.driver.web)
    }
    */

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

sqldelight {
  databases {
    create("AppDatabase") {
      packageName.set("at.mocode.frontend.core.localdb")
      generateAsync.set(true) // WICHTIG: Async-First für JS Support
    }
  }
}
