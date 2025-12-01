plugins {
    // KORREKTUR: Alle Plugins werden jetzt konsistent über den Version Catalog geladen.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Der springBoot-Block konfiguriert die Anwendung, wenn sie als JAR-Datei ausgeführt wird.
springBoot {
    mainClass.set("at.mocode.horses.service.HorsesServiceApplicationKt")
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.horses.horsesDomain)
    implementation(projects.horses.horsesApplication)
    implementation(projects.horses.horsesInfrastructure)
    implementation(projects.horses.horsesApi)

    // Infrastruktur-Clients
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.messaging.messagingClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Datenbank-Abhängigkeiten
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.hikari.cp)
    runtimeOnly(libs.postgresql.driver)
    testRuntimeOnly(libs.h2.driver)


    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.logback.classic)  // SLF4J provider for tests
}

tasks.test {
    useJUnitPlatform()
}
