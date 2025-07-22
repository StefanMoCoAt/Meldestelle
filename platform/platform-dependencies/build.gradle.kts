plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(platform(projects.platform.platformBom))

    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("io.github.microutils:kotlin-logging-jvm")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json")
    api("org.jetbrains.kotlinx:kotlinx-datetime")
}
