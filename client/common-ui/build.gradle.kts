plugins {
    kotlin("jvm")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // Core dependencies
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Domain modules
    implementation(projects.events.eventsDomain)
    implementation(projects.horses.horsesDomain)
    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.members.membersDomain)

    // Compose dependencies for Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.materialIconsExtended)

    // AndroidX dependencies are provided by the Compose plugin

    // Ktor Client dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.serializationKotlinxJson)

    // Kotlinx dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    implementation("com.benasher44:uuid:0.8.4")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
