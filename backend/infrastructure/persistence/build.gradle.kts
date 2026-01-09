plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinJpa)
}

dependencies {
    implementation(projects.core.coreUtils)
    implementation(projects.core.coreDomain)
    implementation(projects.platform.platformDependencies)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)

    // Logging
    implementation(libs.slf4j.api)
}
