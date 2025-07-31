// Dieses Modul definiert die provider-agnostische API für den Event Store.
// Es enthält die Interfaces (z.B. `EventStore`, `EventPublisher`) und die
// Domänen-Events aus `core-domain`, die gespeichert und publiziert werden.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))

    // Abhängigkeit zu den Core-Modulen, um auf Domänenobjekte (Events)
    // und technische Hilfsklassen zugreifen zu können.
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}
