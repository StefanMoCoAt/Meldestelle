plugins {
    kotlin("jvm")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    // Greift explizit auf den "desktop" (JVM) Teil unseres KMP-Moduls zu.
    implementation(projects.client.commonUi)

    // Stellt die Desktop-spezifischen Teile von Jetpack Compose bereit.
    implementation(compose.desktop.currentOs)

    // Stellt die Coroutine-Integration für die Swing-UI-Bibliothek bereit.
    implementation(libs.kotlinx.coroutines.swing)

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
}

compose.desktop {
    application {
        mainClass = "at.mocode.client.desktop.MainKt"
    }
}
