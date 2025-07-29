plugins {
    // Standard Kotlin Plugins
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Das Ktor-Plugin wird hier nicht benötigt, da dies der Spring Boot Service ist,
    // der die Ktor-API nur als Bibliothek nutzt.
    // alias(libs.plugins.ktor)

    application
}

// Dieser Block funktioniert jetzt, weil das `springBoot`-Plugin oben aktiviert ist.
springBoot {
    mainClass.set("at.mocode.events.service.EventsServiceApplicationKt")
}

dependencies {
    // KORREKTUR: Alle Spring-Boot-Abhängigkeiten sollten über den Version Catalog
    // als "starter" deklariert werden. Die Versionen werden dann automatisch
    // durch das Spring Boot Plugin und unser `platform-dependencies`-Modul verwaltet.

    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreUtils)

    implementation(projects.events.eventsDomain)
    implementation(projects.events.eventsApplication)
    implementation(projects.events.eventsInfrastructure)
    implementation(projects.events.eventsApi)

    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.messaging.messagingClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // Spring Boot Starters (aus dem Version Catalog)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    // implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui") // Besser, dies auch in den Katalog aufzunehmen

    // Datenbank-Treiber (aus dem Version Catalog)
    runtimeOnly(libs.postgresql.driver)

    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.spring.boot.starter.test)
}
