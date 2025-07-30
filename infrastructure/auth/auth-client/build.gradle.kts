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
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.auth0.java.jwt)

    testImplementation(projects.platform.platformTesting)
}
