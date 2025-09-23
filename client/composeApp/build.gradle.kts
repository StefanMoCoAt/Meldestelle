import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.composeHotReload)
}

// Project version configuration
version = "1.0.0"
group = "at.mocode"


kotlin {

    // Configure JVM toolchain for all JVM targets
    jvmToolchain(21)

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

            implementation(projects.client.shared)
            // Core Compose Dependencies - minimiert für kleinere Bundle-Größe
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // UiToolingPreview nur für Development, nicht für Production WASM
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
