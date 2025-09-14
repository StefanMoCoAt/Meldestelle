import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync
import org.gradle.api.file.DuplicatesStrategy

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    js(IR) {
        // Disable browser-based tests (Karma/Chrome) to avoid ChromeHeadless issues
        browser {
            testTask {
                // Prevent launching ChromeHeadless (snap permission issues on some systems)
                enabled = false
            }
        }
        // Run JS tests in Node.js instead (no browser needed)
        nodejs {
            testTask {
                useMocha()
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        // Disable browser-based tests for WASM as well to avoid Karma/Chrome
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // HTTP client dependencies for ping-service
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

// ------------------------------------------------------------------
// Fix duplicate Skiko runtime files being copied from jsMain and jsTest
// during JS test packaging by excluding them from jsTest resources and
// making Sync tasks ignore duplicates.
// ------------------------------------------------------------------

// Exclude Skiko runtime files from jsTest processed resources
// to prevent overwriting logs during test packaging.
@Suppress("UNUSED_VARIABLE")
val configureJsTestResources = run {
    // Configure only if the task exists (JS target present)
    tasks.matching { it.name == "jsTestProcessResources" && it is Copy }.configureEach {
        (this as Copy).exclude("skiko.*", "skikod8.mjs")
    }
}

// Also apply the same exclusion for WASM JS test resources, if present
@Suppress("UNUSED_VARIABLE")
val configureWasmJsTestResources = run {
    tasks.matching { it.name == "wasmJsTestProcessResources" && it is Copy }.configureEach {
        (this as Copy).exclude("skiko.*", "skikod8.mjs")
    }
}

// Ensure Kotlin/JS generated Sync tasks do not overwrite duplicates noisily
@Suppress("UNUSED_VARIABLE")
val configureJsCompileSync = run {
    tasks.matching { it.name.endsWith("CompileSync") && it is Sync }.configureEach {
        (this as Sync).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

compose.desktop {
    application {
        mainClass = "at.mocode.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "at.mocode"
            packageVersion = "1.0.0"
        }
    }
}
