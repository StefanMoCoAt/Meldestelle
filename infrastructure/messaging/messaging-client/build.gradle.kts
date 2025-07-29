plugins {
//    kotlin("jvm")
//    kotlin("plugin.spring")

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Dependency Management für konsistente Spring-Versionen
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    implementation(projects.platform.platformDependencies)
    implementation(projects.infrastructure.messaging.messagingConfig)

    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation(projects.platform.platformTesting)
}
