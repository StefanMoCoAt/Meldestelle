plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvm {
    compilerOptions {
      freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
  }

  js(IR) {
    binaries.library()
    // Re-enabled browser environment after Root NodeJs fix
    browser {
      testTask {
        enabled = false
      }
    }
  }

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    binaries.library()
    browser()
  }

  sourceSets {
    all {
      languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
      languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    commonMain.dependencies {
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.datetime)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }

    jsMain.dependencies {
      api(libs.kotlinx.coroutines.core)
    }

    jsTest.dependencies {
      implementation(libs.kotlin.test)
    }

    jvmMain.dependencies {
    }

    jvmTest.dependencies {
      implementation(libs.junit.jupiter.api)
      implementation(libs.mockk)
      implementation(projects.platform.platformTesting)
      implementation(libs.junit.jupiter.api)
      implementation(libs.junit.jupiter.engine)
      implementation(libs.junit.jupiter.params)
      implementation(libs.junit.platform.launcher)
      implementation(libs.mockk)
      implementation(libs.assertj.core)
      implementation(libs.kotlinx.coroutines.test)
    }

  }
}

tasks.named<Test>("jvmTest") {
  useJUnitPlatform()
}
