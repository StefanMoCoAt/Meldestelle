@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

// Project version configuration
version = "1.0.0"
group = "at.mocode"

// Build performance optimizations
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
//    compilerOptions {
//        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
//        freeCompilerArgs.addAll(
//            "-opt-in=kotlin.RequiresOptIn",
//            "-Xjvm-default=all"     // Generate default methods for interfaces (JVM performance)
//        )
//    }
//}

kotlin {
//    // Configure JVM toolchain for all JVM targets
//    jvmToolchain(21)
//
//    // Global compiler options for all targets
//    compilerOptions {
//        freeCompilerArgs.add("-Xexpect-actual-classes")
//    }

//    jvm {
//        compilations.all {
//            compileTaskProvider.configure {
//                compilerOptions {
//                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
//                    freeCompilerArgs.addAll(
//                        "-Xjsr305=strict",
//                        "-Xcontext-parameters"
//                    )
//                }
//            }
//        }
//    }

//    js(IR) {
//        browser {
//            commonWebpackConfig {
//                outputFileName = "meldestelle-client.js"
//                cssSupport {
//                    enabled.set(true)
//                }
//                // Webpack performance optimizations for smaller bundles
//                devServer?.apply {
//                    open = false
//                    port = 8080
//                }
//            }
//            runTask {
//                // Development optimizations
//                args.add("--mode=development")
//                //args.add("--optimization-minimize=false")
//            }
//            webpackTask {
//                // Production optimizations
//                args.add("--mode=production")
//                args.add("--optimization-minimize")
//            }
//            testTask {
//                // Disable browser tests due to ChromeHeadless permission issues
//                enabled = false
//            }
//        }
//
//        // Use Node.js for testing instead of browser
//        nodejs {
//            testTask {
//                useMocha {
//                    timeout = "10s"
//                }
//            }
//        }
//
//        binaries.executable()
//    }

//    wasmJs {
//        browser {
//            commonWebpackConfig {
//                outputFileName = "meldestelle-wasm.js"
//                cssSupport {
//                    enabled.set(true)
//                }
//                // WASM-specific webpack optimizations handled by webpack.config.d files
//                devServer?.apply {
//                    open = false
//                    port = 8080
//                }
//            }
//            runTask {
//                // Development optimizations for WASM
//                args.add("--mode=development")
//                //args.add("--optimization-minimize=false")
//                // Dev server settings handled by webpack.config.d/dev-server.js
//            }
//            webpackTask {
//                // Production optimizations for WASM
//                args.add("--mode=production")
//                args.add("--optimization-minimize")
//            }
//            testTask {
//                // Disable WASM browser tests due to environment issues
//                enabled = false
//            }
//        }
//
//        // WASM-specific compiler optimizations for smaller bundles
//        compilations.all {
//            compileTaskProvider.configure {
//                compilerOptions {
//                    freeCompilerArgs.addAll(
//                        "-Xwasm-use-new-exception-proposal",
//                        "-Xwasm-debugger-custom-formatters",
//                        "-Xwasm-enable-array-range-checks",
//                        "-Xwasm-generate-wat=false",
//                        "-opt-in=kotlin.ExperimentalStdlibApi",
//                        "-opt-in=kotlin.js.ExperimentalJsExport"
//                    )
//                }
//            }
//        }
//
//        binaries.executable()
//    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
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

            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

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
            implementation(libs.kotlinx.coroutines.swing)
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
//@Suppress("UNUSED_VARIABLE")
//val configureJsTestResources = run {
//    // Configure only if the task exists (JS target present)
//    tasks.matching { it.name == "jsTestProcessResources" && it is Copy }.configureEach {
//        (this as Copy).exclude("skiko.*", "skikod8.mjs")
//    }
//}
//
//// Also apply the same exclusion for WASM JS test resources, if present
//@Suppress("UNUSED_VARIABLE")
//val configureWasmJsTestResources = run {
//    tasks.matching { it.name == "wasmJsTestProcessResources" && it is Copy }.configureEach {
//        (this as Copy).exclude("skiko.*", "skikod8.mjs")
//    }
//}
//
//// Ensure Kotlin/JS generated Sync tasks do not overwrite duplicates noisily
//@Suppress("UNUSED_VARIABLE")
//val configureJsCompileSync = run {
//    tasks.matching { it.name.endsWith("CompileSync") && it is Sync }.configureEach {
//        (this as Sync).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    }
//}

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
