plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Dependency Management für konsistente Spring-Versionen
    alias(libs.plugins.spring.dependencyManagement)
}

// Dieser Block funktioniert jetzt, weil das `springBoot`-Plugin oben aktiviert ist.
springBoot {
    mainClass.set("at.mocode.events.service.EventsServiceApplicationKt")
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreUtils)
    implementation(projects.events.eventsDomain)
    implementation(projects.events.eventsApplication)
    implementation(projects.events.eventsInfrastructure)
    implementation(projects.events.eventsApi)

    // Infrastruktur-Clients
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.messaging.messagingClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Datenbank-Treiber
    runtimeOnly(libs.postgresql.driver)

    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.logback.classic)  // SLF4J provider for tests
}

tasks.test {
    useJUnitPlatform()
}
