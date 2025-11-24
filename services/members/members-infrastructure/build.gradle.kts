plugins {
//    kotlin("jvm")
//    kotlin("plugin.spring")
//    kotlin("plugin.jpa") version "2.1.21"

    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Dependency Management für konsistente Spring-Versionen
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    api(platform(projects.platform.platformBom))

    implementation(projects.services.members.membersDomain)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.cache.cacheApi)
    implementation(projects.infrastructure.eventStore.eventStoreApi)
    implementation(projects.infrastructure.messaging.messagingClient)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")

    testImplementation(projects.platform.platformTesting)
}
