# Frontend-Architektur "Meldestelle Portal"

Dieses Verzeichnis dokumentiert die Architektur und die technischen Details des KMP-Frontends "Meldestelle Portal".

## Übersicht

Das Frontend ist eine **Kotlin Multiplatform (KMP)**-Anwendung, die für die folgenden Ziele entwickelt wird:
*   **Desktop (JVM):** Eine eigenständige Desktop-Anwendung.
*   **Web (JS/Wasm):** Eine moderne Web-Anwendung, die im Browser läuft.

Die Architektur ist auf **Offline-Fähigkeit** und eine reaktive UI ausgelegt.

## Modul-Struktur

Das `frontend`-Verzeichnis ist wie folgt strukturiert, um eine klare Trennung der Verantwortlichkeiten zu gewährleisten:

*   `shells/`: Die ausführbaren Anwendungen (Assembler-Module), die die App für eine bestimmte Plattform (Desktop, Web) zusammenbauen.
*   `features/`: Vertikale Slices der Anwendung. Jedes Feature-Modul kapselt eine bestimmte Funktionalität (z.B. `auth-feature`, `ping-feature`). Wichtig: Ein Feature-Modul darf niemals von einem anderen Feature-Modul abhängen.
*   `core/`: Gemeinsame Basis-Module, die von allen Features genutzt werden. Dazu gehören:
    *   `design-system/`: Compose-Komponenten, Themes, Farben.
    *   `domain/`: Fachliche Kernlogik und Datenmodelle des Frontends.
    *   `local-db/`: SQLDelight-Datenbank-Setup und Queries.
    *   `navigation/`: Navigations-Logik und Routen-Definitionen.
    *   `network/`: Ktor-Client und API-Definitionen.

## Kerntechnologien

*   **UI:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) für eine deklarative, plattformübergreifende UI.
*   **Persistenz (Offline-First):** [SQLDelight](https://cashapp.github.io/sqldelight/) für die lokale Speicherung von Daten.
*   **State Management:** Kotlin Coroutines und Flow in Kombination mit ViewModels.
*   **Dependency Injection:** [Koin](https://insert-koin.io/) für die lose Kopplung von Komponenten.
*   **Netzwerk:** [Ktor Client](https://ktor.io/docs/client-introduction.html) für die Kommunikation mit dem Backend.

## Wichtige Dokumente

*   **[ADR-0010: SQLDelight für Cross-Platform-Persistenz](../01_Architecture/adr/0010-sqldelight-for-cross-platform-persistence.md):** Beschreibt die Entscheidung für SQLDelight.
*   **[ADR-0011: Koin für Dependency Injection](../01_Architecture/adr/0011-koin-for-dependency-injection.md):** Beschreibt die Entscheidung für Koin.
*   **[Offline-First-Architektur](offline-first-architecture.md):** Detaillierte Beschreibung der Offline-First-Strategie.
*   **[Web-Setup (Webpack & Worker)](web-setup.md):** Details zur Konfiguration des Web-Targets.
