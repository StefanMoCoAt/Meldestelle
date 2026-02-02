import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Dieses Modul kapselt die gesamte UI und Logik für das Ping-Feature.
 */
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

group = "at.mocode.clients"
version = "1.0.0"

kotlin {
  jvm()
  js {
    binaries.library()
    browser {
        testTask { enabled = false }
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.contracts.pingApi)
      implementation(projects.frontend.core.designSystem)
      implementation(projects.frontend.core.sync)
      implementation(projects.frontend.core.localDb)
      implementation(libs.sqldelight.coroutines)
      implementation(projects.frontend.core.domain)

      implementation(compose.foundation)
      implementation(compose.runtime)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      implementation(libs.bundles.kmp.common)
      implementation(libs.bundles.ktor.client.common)
      implementation(libs.bundles.compose.common)

      implementation(libs.koin.core)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    jvmTest.dependencies {
      implementation(libs.mockk)
      implementation(projects.platform.platformTesting)
      implementation(libs.bundles.testing.jvm)
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
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn"
    )
  }
}
