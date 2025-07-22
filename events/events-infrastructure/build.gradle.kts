plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa") version "2.1.20"
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.events.eventsDomain)
    implementation(projects.events.eventsApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.cache.cacheApi)
    implementation(projects.infrastructure.eventStore.eventStoreApi)
    implementation(projects.infrastructure.messaging.messagingClient)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")

    testImplementation(projects.platform.platformTesting)
}
