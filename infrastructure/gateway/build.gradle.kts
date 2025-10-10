// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR
springBoot {
    mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
}

dependencies {
    // Platform BOM für zentrale Versionsverwaltung
    implementation(platform(projects.platform.platformBom))

    // Core project dependencies (sind korrekt)
    implementation(projects.core.coreUtils)
    implementation(projects.platform.platformDependencies)

    // === BEREINIGTE ABHÄNGIGKEITEN ===

    // 1. Spring Cloud Gateway & Service Discovery (dies ist die KERN-Abhängigkeit)
    implementation(libs.bundles.spring.cloud.gateway)

    // 2. Spring Boot Security (ersetzt das "service.complete"-Bundle)
    // Dieses Bundle sollte spring-boot-starter-security, oauth2-client, oauth2-resource-server etc. enthalten
    // Temporär auskommentieren, um das Bundle als Fehlerquelle auszuschließen
    // implementation(libs.bundles.spring.boot.security)

    // Stattdessen die Abhängigkeiten direkt hinzufügen:
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)


    // 3. Resilience4j & AOP für Circuit Breaker
    implementation(libs.bundles.resilience)
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // 4. Monitoring-Client (ist korrekt)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // 5. Auth-Client für JWT-Erstellung/Service (falls noch benötigt nach Schritt 2)
    implementation(projects.infrastructure.auth.authClient)

    // 6. Logging & Jackson (sind korrekt)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.jackson.kotlin)

    // FÜGEN SIE DIESE ZEILE HINZU, UM DIE FEHLER ZU BEHEBEN:
    implementation(libs.spring.boot.starter.actuator)

    // Test-Abhängigkeiten (sind korrekt)
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}

// Konfiguration für Integration Tests
sourceSets {
    val integrationTest by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Test>("integrationTest") {
    description = "Führt die Integration Tests aus"
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()

    shouldRunAfter("test")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
