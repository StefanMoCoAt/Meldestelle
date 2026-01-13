# Frontend-Architektur "Meldestelle Portal"

Dieses Verzeichnis dokumentiert die Architektur und die technischen Details des KMP-Frontends "Meldestelle Portal".

## Übersicht

Das Frontend ist eine **Kotlin Multiplatform (KMP)**-Anwendung, die für die folgenden Ziele entwickelt wird:
*   **Desktop (JVM):** Eine eigenständige Desktop-Anwendung.
*   **Web (JS/Wasm):** Eine moderne Web-Anwendung, die im Browser läuft.

Die Architektur ist auf **Offline-Fähigkeit** und eine reaktive UI ausgelegt.

## Kerntechnologien

*   **UI:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) für eine deklarative, plattformübergreifende UI.
*   **Persistenz (Offline-First):** [SQLDelight](https://cashapp.github.io/sqldelight/) für die lokale Speicherung von Daten.
*   **State Management:** Kotlin Coroutines und Flow in Kombination mit ViewModels.
*   **Dependency Injection:** [Koin](https://insert-koin.io/) für die lose Kopplung von Komponenten.
*   **Netzwerk:** [Ktor Client](https://ktor.io/docs/client-introduction.html) für die Kommunikation mit dem Backend.

## Architektur-Prinzipien

*   **Clean Architecture / DDD:** Die Codebasis ist in Schichten unterteilt (UI, Application, Domain, Infrastructure), um eine klare Trennung der Verantwortlichkeiten zu gewährleisten.
*   **Async-First Data Layer:** Alle Datenbank- und Netzwerk-Interaktionen sind asynchron (`suspend`-Funktionen), um die UI nicht zu blockieren.
*   **Feature-basierte Modularisierung:** Die Anwendung ist in unabhängige "Feature"-Module unterteilt, die jeweils eine bestimmte Funktionalität kapseln.

## Wichtige Dokumente

*   **[ADR-0010: SQLDelight für Cross-Platform-Persistenz](../01_Architecture/adr/0010-sqldelight-for-cross-platform-persistence.md):** Beschreibt die Entscheidung für SQLDelight.
*   **[ADR-0011: Koin für Dependency Injection](../01_Architecture/adr/0011-koin-for-dependency-injection.md):** Beschreibt die Entscheidung für Koin.
*   **[Offline-First-Architektur](offline-first-architecture.md):** Detaillierte Beschreibung der Offline-First-Strategie.
*   **[Web-Setup (Webpack & Worker)](web-setup.md):** Details zur Konfiguration des Web-Targets.
