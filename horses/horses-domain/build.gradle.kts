plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    testImplementation(projects.platform.platformTesting)
}
