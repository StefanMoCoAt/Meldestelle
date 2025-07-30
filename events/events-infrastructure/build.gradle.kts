plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
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

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.postgresql.driver)

    testImplementation(projects.platform.platformTesting)
}
