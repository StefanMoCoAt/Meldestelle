plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "at.mocode"
version = "1.0.0"

// Anwendungskonfiguration
application {
    mainClass.set("at.mocode.ApplicationKt")

    // JVM-Argumente für optimale Performance und Entwicklung
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}",
        "-XX:+UseG1GC",                    // G1 Garbage Collector für bessere Performance
        "-XX:MaxGCPauseMillis=100",        // Maximale GC-Pausenzeit
        "-Djava.awt.headless=true"         // Headless-Modus für Server-Umgebung
    )
}

dependencies {
    // === PROJEKT-ABHÄNGIGKEITEN ===
    implementation(projects.shared)

    // === KOTLIN CORE BIBLIOTHEKEN ===
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.uuid)
    implementation(libs.bignum)

    // === KTOR SERVER CORE ===
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)

    // === KTOR SERVER PLUGINS ===
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serializationKotlinxJson)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)

    // === DATENBANK - EXPOSED ORM ===
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlinDatetime)

    // === CONNECTION POOLING ===
    implementation(libs.hikari.cp)

    // === LOGGING ===
    implementation(libs.logback)

    // === DATENBANKTREIBER ===
    runtimeOnly(libs.postgresql.driver)    // PostgreSQL für Produktion
    runtimeOnly(libs.h2.driver)           // H2 für Entwicklung und Tests

    // === TESTING ===
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junitJupiter)
}
