plugins {
//    kotlin("jvm")
//    kotlin("plugin.spring")

    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Dependency Management für konsistente Spring-Versionen
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.services.members.membersDomain)
    implementation(projects.services.members.membersApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.messaging.messagingClient)

    implementation("org.springframework:spring-web")
    implementation("org.springdoc:springdoc-openapi-starter-common")
    // Security/JWT for extracting claims from principal
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    testImplementation(projects.platform.platformTesting)
}
