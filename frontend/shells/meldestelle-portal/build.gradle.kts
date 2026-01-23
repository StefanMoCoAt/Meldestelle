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
//                enabled = false

        useKarma {
          useChromeHeadless()
          environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
        }
      }
    }
    binaries.executable()
  }

  // Wasm vorerst deaktiviert
  /*
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }
  */

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
    }

    /*
    val wasmJsMain = getByName("wasmJsMain")
    wasmJsMain.dependencies {
      implementation(libs.ktor.client.js) // WASM verwendet JS-Client [cite: 7]

      // Compose für shared UI components für WASM
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
    }
    */

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

// ---------------------------------------------------------------------------
// SQLDelight WebWorker (OPFS) resource
// ---------------------------------------------------------------------------
// `:frontend:core:local-db` ships `sqlite.worker.js` as a JS resource.
// When bundling the final JS app, webpack resolves `new URL("sqlite.worker.js", import.meta.url)`
// relative to the Kotlin JS package folder (root build dir). We therefore copy the worker into
// that folder before webpack runs.

val copySqliteWorkerJs by tasks.registering(Copy::class) {
  val localDb = project(":frontend:core:local-db")
  dependsOn(localDb.tasks.named("jsProcessResources"))

  from(localDb.layout.buildDirectory.file("processedResources/js/main/sqlite.worker.js"))

  // Root build directory where Kotlin JS packages are assembled.
  // Use a concrete path (instead of a Provider) so the Copy task always materializes the directory.
  // The package name is constructed from the project path: Meldestelle-frontend-shells-meldestelle-portal
  // Note: We use rootProject.layout.buildDirectory because Kotlin JS plugin puts packages in root build dir.
  into(rootProject.layout.buildDirectory.dir("js/packages/${rootProject.name}-frontend-shells-meldestelle-portal/kotlin"))
}

// Ensure the worker is present for the production bundle.
tasks.named("jsBrowserProductionWebpack") {
  dependsOn(copySqliteWorkerJs)
}

// Ensure the worker is present for the development bundle.
tasks.named("jsBrowserDevelopmentWebpack") {
  dependsOn(copySqliteWorkerJs)
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
// The remaining Kotlin/Gradle message
// `Cannot rewrite paths in JavaScript source maps: Too many sources or format is not supported`
// is treated as an external Kotlin/JS toolchain limitation and is documented separately.

// Configure a duplicate handling strategy for distribution tasks
tasks.withType<Tar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Duplicate-Handling für Distribution
tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Statt EXCLUDE
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
