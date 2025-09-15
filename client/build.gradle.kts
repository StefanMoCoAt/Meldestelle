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

// Project version configuration
version = "1.0.0"
group = "at.mocode"

// Build performance optimizations
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"     // Generate default methods for interfaces (JVM performance)
        )
    }
}

kotlin {
    // Configure JVM toolchain for all JVM targets
    jvmToolchain(21)

    // Global compiler options for all targets
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                    freeCompilerArgs.addAll(
                        "-Xjsr305=strict",
                        "-Xcontext-parameters"
                    )
                }
            }
        }
    }

    js(IR) {
        // Disable browser-based tests (Karma/Chrome) to avoid ChromeHeadless issues
        browser {
            commonWebpackConfig {
                outputFileName = "meldestelle-client.js"
                // Enable CSS support and optimization
                cssSupport {
                    enabled.set(true)
                }
            }
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
                outputFileName = "meldestelle-wasm.js"
                // Enable CSS support for better bundling
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                enabled = false
            }
        }
        binaries.executable()
        // WASM-specific compiler optimizations for smaller bundles
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-Xwasm-use-new-exception-proposal",  // Use efficient WASM exception handling
                        "-Xwasm-debugger-custom-formatters",  // Optimize debug info for smaller size
                        "-Xwasm-enable-array-range-checks",   // Optimize array bounds checking
                        "-Xwasm-generate-wat=false",          // Skip WAT generation for smaller output
                        "-Xwasm-target=wasm32",               // Explicit WASM32 target
                        "-opt-in=kotlin.ExperimentalStdlibApi", // Enable stdlib optimizations
                        "-opt-in=kotlin.js.ExperimentalJsExport" // Enable JS export optimizations
                    )
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core Compose Dependencies - minimiert für kleinere Bundle-Größe
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // UiToolingPreview nur für Development, nicht für Production WASM
            // implementation(compose.components.uiToolingPreview)

            // HTTP client dependencies for ping-service - optimiert
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
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
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
            packageName = "Meldestelle"
            packageVersion = "1.0.0"

            // Application metadata
            description = "Pferdesport Meldestelle System - Client Application"
            copyright = "© 2025 Meldestelle Project"
            vendor = "at.mocode"

            // Platform-specific configurations
            linux {
                iconFile.set(project.file("src/commonMain/resources/icon.png"))
                packageName = "meldestelle"
                debMaintainer = "stefan@mocode.at"
                menuGroup = "Office"
            }

            windows {
                iconFile.set(project.file("src/commonMain/resources/icon.ico"))
                menuGroup = "Meldestelle"
                upgradeUuid = "61DAB35E-17CB-43B8-8A72-39876CF0E021"
            }

            macOS {
                iconFile.set(project.file("src/commonMain/resources/icon.icns"))
                bundleID = "at.mocode.meldestelle"
                packageBuildVersion = "1.0.0"
                packageVersion = "1.0.0"
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}
