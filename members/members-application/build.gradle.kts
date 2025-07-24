plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.members.membersDomain)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.messaging.messagingClient)
    testImplementation(projects.platform.platformTesting)
}
