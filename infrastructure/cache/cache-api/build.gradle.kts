plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.platform.platformDependencies)
    testImplementation(projects.platform.platformTesting)
}
