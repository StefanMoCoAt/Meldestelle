plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("at.mocode.members.service.MembersServiceApplicationKt")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.members.membersDomain)
    implementation(projects.members.membersApplication)
    implementation(projects.members.membersInfrastructure)
    implementation(projects.members.membersApi)

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
