plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.events.eventsDomain)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    testImplementation(projects.platform.platformTesting)
}
