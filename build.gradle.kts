// root/build.gradle.kts
plugins {
    // Apply base plugin to provide lifecycle tasks like assemble, build, clean
    base
    // Dies ist notwendig, um zu verhindern, dass die Plugins mehrfach geladen werden
    // im Classloader jedes Subprojekts
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Wrapper task configuration for the root project
tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.BIN
}
