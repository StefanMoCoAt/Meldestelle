plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("at.mocode.horses.service.HorsesServiceApplicationKt")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.horses.horsesDomain)
    implementation(projects.horses.horsesApplication)
    implementation(projects.horses.horsesInfrastructure)
    implementation(projects.horses.horsesApi)

    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.messaging.messagingClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // Database dependencies
    implementation("org.jetbrains.exposed:exposed-core")
    implementation("org.jetbrains.exposed:exposed-dao")
    implementation("org.jetbrains.exposed:exposed-jdbc")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime")
    implementation("com.zaxxer:HikariCP")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("com.h2database:h2")

    testImplementation(projects.platform.platformTesting)
}
