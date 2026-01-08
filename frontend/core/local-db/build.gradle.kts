@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.ksp)
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
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
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

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  add("kspCommonMainMetadata", libs.androidx.room.compiler)
  add("kspJvm", libs.androidx.room.compiler)
  // add("kspJs", libs.androidx.room.compiler) // Room compiler support for JS might vary, check specific version support
}
