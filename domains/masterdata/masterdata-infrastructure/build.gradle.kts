plugins {
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

    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.masterdata.masterdataApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.cache.cacheApi)
    implementation(projects.infrastructure.eventStore.eventStoreApi)
    implementation(projects.infrastructure.messaging.messagingClient)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")

    testImplementation(projects.platform.platformTesting)
}
