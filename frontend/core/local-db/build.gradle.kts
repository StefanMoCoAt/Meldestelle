@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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

  sourceSets {
    commonMain.dependencies {
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }

    jvmMain.dependencies {
    }

    jsMain.dependencies {
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
