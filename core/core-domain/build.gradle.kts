plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.platform.platformDependencies)

    // UUID handling
    api("com.benasher44:uuid:0.8.2")

    // Serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-json")
    api("org.jetbrains.kotlinx:kotlinx-datetime")

    testImplementation(projects.platform.platformTesting)
}
