@file:OptIn(ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("DEPRECATION")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
  // Toolchain is now handled centrally in the root build.gradle.kts

  // JVM Target für Desktop
  jvm {
    binaries {
      executable {
        mainClass.set("MainKt")
      }
    }
  }

  // JavaScript Target für Web
  js(IR) {
    browser {
      commonWebpackConfig {
        cssSupport { enabled = true }
        // Webpack-Mode abhängig von Build-Typ
        mode = if (project.hasProperty("production"))
          KotlinWebpackConfig.Mode.PRODUCTION
        else
          KotlinWebpackConfig.Mode.DEVELOPMENT
      }

      webpackTask {
        mainOutputFileName = "web-app.js"
      }

      // Development Server konfigurieren
      runTask {
        mainOutputFileName.set("web-app.js")
      }
      // Browser-Tests komplett deaktivieren (Configuration Cache kompatibel)
      testTask {
        useKarma {
          useChromeHeadless()
          environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
        }
      }
    }
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      // Shared modules
      implementation(projects.frontend.shared)
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

      // Bundles (Cleaned up dependencies)
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
      implementation(devNpm("copy-webpack-plugin", "11.0.0"))
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

// ---------------------------------------------------------------------------
// SQLDelight WebWorker (OPFS) resource
// ---------------------------------------------------------------------------
// `:frontend:core:local-db` ships `sqlite.worker.js` as a JS resource.
// We need to ensure this worker file is available in the output directory so the browser can load it.
// The WASM file itself is handled by Webpack (via CopyWebpackPlugin in webpack.config.d/sqlite-config.js).

val copySqliteWorkerToWebpackSource by tasks.registering(Copy::class) {
  val localDb = project(":frontend:core:local-db")
  dependsOn(localDb.tasks.named("jsProcessResources"))

  from(localDb.layout.buildDirectory.file("processedResources/js/main/sqlite.worker.js"))

  // Root build directory where Kotlin JS packages are assembled.
  // This is one of the directories served by webpack-dev-server for static content.
  into(rootProject.layout.buildDirectory.dir("js/packages/${rootProject.name}-frontend-shells-meldestelle-portal/kotlin"))
}

// Ensure the worker is present for the development bundle.
tasks.named("jsBrowserDevelopmentWebpack") {
  dependsOn(copySqliteWorkerToWebpackSource)
}

// Ensure the worker is present for the production bundle.
tasks.named("jsBrowserProductionWebpack") {
  dependsOn(copySqliteWorkerToWebpackSource)
}

// KMP Compile-Optionen
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn",
      "-Xskip-metadata-version-check", // Für bleeding-edge Versionen
      // Suppress beta warning for expect/actual declarations used in this module
      "-Xexpect-actual-classes"
    )
  }
}

// ---------------------------------------------------------------------------
// Kotlin/JS source maps
// ---------------------------------------------------------------------------
// Production source maps must remain enabled for browser debugging.

// Configure a duplicate handling strategy for distribution tasks
tasks.withType<Tar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Duplicate-Handling für Distribution
tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Sync> {
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
