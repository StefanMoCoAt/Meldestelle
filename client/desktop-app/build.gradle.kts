import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm {
        compilations.all {
//            compileTaskProvider.configure{
//                compilerOptions {
//                    freeCompilerArgs.add("-Xjvm-default=all")
//                    freeCompilerArgs.add("-Xcontext-receivers")
//                    freeCompilerArgs.add("-Xno-param-assertions")
//                    freeCompilerArgs.add("-Xno-call-assertions")
//                    freeCompilerArgs.add("-Xno-receiver-assertions")
//                    freeCompilerArgs.add("-Xno-optimize")
//                    freeCompilerArgs.add("-Xno-param-assertions")
//                    freeCompilerArgs.add("-Xno-receiver-assertions")
//                    freeCompilerArgs.add("-Xno-optimize")
//                    freeCompilerArgs.add("-Xno-check-impl")
//                    freeCompilerArgs.add("-Xno-optimize")
//                }
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Project dependencies
                implementation(project(":client:common-ui"))

                // Compose Desktop
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.uiTooling)
                implementation(compose.runtime)
                implementation(compose.foundation)

                // Serialization support
                implementation(libs.kotlinx.serialization.json)

                // HTTP Client & Coroutines
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)
                implementation(libs.kotlinx.coroutines.swing)

                // Logging
                implementation(libs.kotlin.logging.jvm)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.testing.jvm)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "at.mocode.client.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Meldestelle Desktop"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
            }
        }
    }
}
