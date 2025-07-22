plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(projects.client.commonUi)
    implementation(projects.client.webApp)
    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.cache.redisCache)
    implementation(projects.infrastructure.eventStore.redisEventStore)

    // Domain modules
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.events.eventsDomain)
    implementation(projects.horses.horsesDomain)
    implementation(projects.masterdata.masterdataDomain)

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter")

    // Redis dependencies
    implementation("org.redisson:redisson:3.27.1")
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

    // Kotlinx dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.benasher44:uuid:0.8.4")

    // Compose dependencies
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.materialIconsExtended)

    testImplementation(projects.platform.platformTesting)
}
