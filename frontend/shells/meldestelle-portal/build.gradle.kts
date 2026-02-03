@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("DEPRECATION")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

/**
 * Dieses Modul ist der "Host". Es kennt alle Features und die Shared-Module und
 * setzt sie zu einer lauffähigen Anwendung zusammen.
 */
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  // JVM Target für Desktop
  jvm {
    binaries {
      executable {
        mainClass.set("MainKt")
      }
    }
  }

  // JavaScript Target für Web
  js {
    browser {
      commonWebpackConfig {
        cssSupport { enabled = true }
        // Webpack-Mode abhängig von Build-Typ
        mode = if (project.hasProperty("production"))
          KotlinWebpackConfig.Mode.PRODUCTION
        else
          KotlinWebpackConfig.Mode.DEVELOPMENT

        // Source Maps Optimierung für Docker Builds
        if (project.hasProperty("noSourceMaps")) {
          sourceMaps = false
        }
      }

      webpackTask {
        mainOutputFileName = "web-app.js"
      }

      // Development Server konfigurieren
      runTask {
        mainOutputFileName.set("web-app.js")
      }
    }
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      // Shared modules
      implementation(projects.frontend.core.domain)
      implementation(projects.frontend.core.designSystem)
      implementation(projects.frontend.core.navigation)
      implementation(projects.frontend.core.network)
      implementation(projects.frontend.core.sync)
      implementation(projects.frontend.core.localDb)
      implementation(projects.frontend.core.auth)
      implementation(projects.frontend.features.pingFeature)

      // DI (Koin) needed to call initKoin { modules(...) }
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)

      // Compose Multiplatform
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Bundles
      implementation(libs.bundles.kmp.common)        // Coroutines, Serialization, DateTime
      implementation(libs.bundles.compose.common)     // ViewModel & Lifecycle
    }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.koin.core)
    }

    jsMain.dependencies {
      implementation(compose.html.core)
      // Benötigt für custom webpack config (wasm.js)
      implementation(devNpm("copy-webpack-plugin", libs.versions.copyWebpackPlugin.get()))
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

// Duplicate-Handling für Distribution (Zentralisiert in Root build.gradle.kts, aber hier spezifisch für Distribution Tasks)
tasks.withType<Tar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Desktop Application Configuration
compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Meldestelle"
      packageVersion = "1.0.0"
      description = "Meldestelle Development App"
    }
  }
}
