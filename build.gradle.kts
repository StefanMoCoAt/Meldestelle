// root/build.gradle.kts
plugins {
    // Dies ist notwendig, um zu verhindern, dass die Plugins mehrfach geladen werden
    // im Classloader jedes Subprojekts
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
}
