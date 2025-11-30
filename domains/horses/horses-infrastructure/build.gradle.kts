plugins {
    // KORREKTUR: Alle Plugins werden jetzt konsistent über den Version Catalog geladen.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    // Das JPA-Plugin wird jetzt ebenfalls zentral verwaltet.
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.horses.horsesDomain)
    implementation(projects.horses.horsesApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.cache.cacheApi)
    implementation(projects.infrastructure.eventStore.eventStoreApi)
    implementation(projects.infrastructure.messaging.messagingClient)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring Data JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // Datenbank-Treiber
    runtimeOnly(libs.postgresql.driver)

    // Testing
    testImplementation(projects.platform.platformTesting)
}
