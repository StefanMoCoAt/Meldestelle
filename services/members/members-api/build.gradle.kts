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

    implementation(projects.members.membersDomain)
    implementation(projects.members.membersApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.messaging.messagingClient)

    implementation("org.springframework:spring-web")
    implementation("org.springdoc:springdoc-openapi-starter-common")

    testImplementation(projects.platform.platformTesting)
}
