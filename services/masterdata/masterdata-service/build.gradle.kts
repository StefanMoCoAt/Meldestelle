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
    mainClass.set("at.mocode.masterdata.service.MasterdataServiceApplicationKt")
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreUtils)
    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.masterdata.masterdataApplication)
    implementation(projects.masterdata.masterdataInfrastructure)
    implementation(projects.masterdata.masterdataApi)

    // Infrastruktur-Clients
    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.messaging.messagingClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    //implementation(libs.springdoc.openapi.starter.webmvc.ui)

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
