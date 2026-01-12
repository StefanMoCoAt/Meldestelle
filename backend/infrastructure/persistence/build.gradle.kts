plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot) // Spring Boot Plugin hinzufügen
    alias(libs.plugins.spring.dependencyManagement) // Dependency Management für Spring
}

// Library module: do not create an executable Spring Boot jar here.
tasks.bootJar {
    enabled = false
}

// Ensure a regular jar is produced instead.
tasks.jar {
    enabled = true
}

dependencies {
    implementation(projects.core.coreUtils)
    implementation(projects.core.coreDomain)
    implementation(projects.platform.platformDependencies)

    // Spring Boot Database dependencies
    implementation(libs.bundles.database.complete)

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
