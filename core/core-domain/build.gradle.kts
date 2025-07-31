// Dieses Modul definiert die Kern-Domänenobjekte des Shared Kernels.
// Es enthält keine Implementierungsdetails, nur reine Datenklassen und Enums.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Stellt sicher, dass dieses Modul Zugriff auf die im zentralen Katalog
    // definierten Bibliotheken hat.
    api(projects.platform.platformDependencies)

    // Kern-Abhängigkeiten für das Domänen-Modell.
    // `api` wird verwendet, damit Services, die `core-domain` einbinden,
    // diese Typen ebenfalls direkt nutzen können.
    api(libs.uuid)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)

    // Stellt die Test-Bibliotheken bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}
