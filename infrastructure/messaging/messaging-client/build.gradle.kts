plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(projects.platform.platformDependencies)
    implementation(projects.infrastructure.messaging.messagingConfig)

    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation(projects.platform.platformTesting)
}
