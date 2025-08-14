// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR und Build-Informationen.
springBoot {
    mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
    buildInfo()
}

// Optimiert Kotlin-Compiler-Einstellungen für bessere Performance.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt Utilities bereit
    implementation(projects.core.coreUtils)
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)

    // Stellt die Spring Cloud Gateway und Consul Discovery Abhängigkeiten bereit
    implementation(libs.bundles.spring.cloud.gateway)
    // Circuit Breaker (Resilience4j) für Gateway Filter - optimiert mit libs reference
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop) // Benötigt für Resilience4j AOP
    // Reaktiver Webserver (Netty) - now properly referenced from libs
    implementation(libs.spring.boot.starter.webflux)
    // Spring Security (WebFlux) – benötigt für SecurityWebFilterChain-Konfiguration
    implementation(libs.spring.boot.starter.security)

    // Bindet die wiederverwendbare Logik zur JWT-Validierung ein.
    implementation(projects.infrastructure.auth.authClient)

    // Bindet die wiederverwendbare Logik für Metriken und Tracing ein.
    implementation(projects.infrastructure.monitoring.monitoringClient)
    // Explizite Actuator-Abhängigkeit für Health Indicators (benötigt für GatewayHealthIndicator)
    // Obwohl bereits im monitoring-client Bundle, wird durch 'implementation' nicht transitiv verfügbar
    implementation(libs.spring.boot.starter.actuator)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    // Redundante Security-Abhängigkeit im Testkontext entfernt (bereits durch platform-testing abgedeckt)

}
