plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("at.mocode.events.service.EventsServiceApplicationKt")
}

dependencies {
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

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(projects.platform.platformTesting)
}
