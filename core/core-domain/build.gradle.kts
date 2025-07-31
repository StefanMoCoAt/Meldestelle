// Dieses Modul definiert die Kern-Domänenobjekte des Shared Kernels.
// Es enthält keine Implementierungsdetails, nur reine Datenklassen und Enums.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xopt-in=kotlin.time.ExperimentalTime")
    }
}

dependencies {
    // Stellt sicher, dass dieses Modul Zugriff auf die im zentralen Katalog
    // definierten Bibliotheken hat.
    api(projects.platform.platformDependencies)

    // Kern-Abhängigkeiten für das Domänen-Modul.
    api(libs.uuid)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)

    // Stellt die Test-Bibliotheken bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}
