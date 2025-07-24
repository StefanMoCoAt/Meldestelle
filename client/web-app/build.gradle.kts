plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
}

repositories {
    google()
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.client.commonUi)
    implementation(projects.infrastructure.auth.authClient)

    // Core modules
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Domain modules
    implementation(projects.members.membersDomain)
    implementation(projects.members.membersApplication)
    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.horses.horsesDomain)
    implementation(projects.events.eventsDomain)

    // Compose dependencies for Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.materialIconsExtended)

    // Kotlinx dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    implementation("com.benasher44:uuid:0.8.4")

    testImplementation(projects.platform.platformTesting)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
