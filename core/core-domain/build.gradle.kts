plugins {
    // Definiert dieses Modul als ein Standard Kotlin/JVM-Modul.
//    kotlin("jvm")
    // Aktiviert das Kotlinx Serialization Plugin, da unsere DTOs und Enums
    // als @Serializable markiert sind.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Stellt sicher, dass dieses Modul Zugriff auf die im zentralen Katalog
    // definierten Bibliotheken hat.
    api(projects.platform.platformDependencies)

    // --- Kern-Abhängigkeiten für das Domänen-Modell ---
    // Diese Bibliotheken definieren die grundlegenden Datentypen unseres Modells.
    // Wir verwenden `api` anstelle von `implementation`, damit Services, die
    // `core-domain` einbinden, diese Typen ebenfalls direkt nutzen können.

    // Stellt den `Uuid`-Typ für unsere eindeutigen IDs bereit.
    api(libs.uuid)

    // Stellt die `kotlinx.serialization`-Engine bereit, insbesondere für JSON.
    api(libs.kotlinx.serialization.json)

    // Stellt moderne Datums- und Zeit-Typen wie `Instant` und `LocalDate` bereit.
    api(libs.kotlinx.datetime)

    // --- Test-Abhängigkeiten ---
    // Stellt die notwendigen Bibliotheken für das Schreiben von Tests bereit.
    // `testImplementation` sorgt dafür, dass diese Bibliotheken nicht Teil
    // des finalen produktiven Codes werden.
    testImplementation(projects.platform.platformTesting)
}
