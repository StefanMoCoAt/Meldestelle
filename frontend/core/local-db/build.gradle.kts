@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  jvm()
  js {
    binaries.library()
    browser()
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
      implementation(npm("@sqlite.org/sqlite-wasm", libs.versions.sqliteWasm.get()))
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
