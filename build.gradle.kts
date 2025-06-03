// Import the NodeJsRootExtension class
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// Define repositories for all projects
allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

// Configure Node.js to use the system-installed executable
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.plugin.KotlinJsPluginWrapper::class.java) {
    rootProject.extensions.configure<NodeJsRootExtension>("nodeJs") {
        nodeVersion = "18.18.2"
        download = false
        nodeCommand = "/usr/bin/node"
    }
}
