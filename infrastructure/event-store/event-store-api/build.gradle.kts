plugins {
    kotlin("jvm")
}

dependencies {
    // Apply platform BOM for version management
    implementation(platform(projects.platform.platformBom))

    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    testImplementation(projects.platform.platformTesting)
}
