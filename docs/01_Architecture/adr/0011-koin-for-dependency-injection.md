# ADR-0011: Koin für Dependency Injection

## Status

Akzeptiert

## Kontext

Das KMP-Frontend benötigt einen Dependency-Injection-Mechanismus, der auf allen Zielplattformen (JVM, JS, Wasm) funktioniert und gut mit Compose Multiplatform integriert ist. Wir müssen sicherstellen, dass die Anwendungslogik (ViewModels, Repositories) lose gekoppelt und leicht testbar ist.

Besonders wichtig ist die zentrale Bereitstellung von plattformspezifischen Implementierungen (z.B. Datenbank-Treiber) und die Verwaltung von Singletons wie dem Ktor HTTP-Client.

## Entscheidung

Wir werden **Koin** als Dependency-Injection-Framework für das KMP-Frontend verwenden.

*   **Koin** ist ein leichtgewichtiger DI-Container, der vollständig in Kotlin geschrieben ist und alle KMP-Ziele unterstützt.
*   **Compose-Integration:** Koin bietet eine nahtlose Integration mit Compose Multiplatform (`koin-compose`), die es uns ermöglicht, ViewModels und andere Abhängigkeiten direkt in unseren Composable-Funktionen zu injizieren.
*   **Modulare Konfiguration:** Wir werden Koin-Module verwenden, um die Abhängigkeiten für jede Schicht unserer Anwendung (Core, Features, Shells) zu definieren. Dies fördert die Modularität und Übersichtlichkeit.
*   **Zentrale Initialisierung:** Die Koin-Anwendung wird in den plattformspezifischen `main`-Funktionen der "Shell"-Module (z.B. `meldestelle-portal`) initialisiert.

## Konsequenzen

*   **Positive:**
    *   **Plattformübergreifende DI:** Wir können dieselbe DI-Konfiguration auf allen Zielplattformen verwenden.
    *   **Vereinfachte Testbarkeit:** Koin erleichtert das Mocking von Abhängigkeiten in Unit-Tests.
    *   **Gute Integration mit Compose:** Die `koin-compose`-Bibliothek reduziert den Boilerplate-Code bei der Injektion von Abhängigkeiten in die UI.
    *   **Schnelle Lernkurve:** Koin ist im Vergleich zu anderen DI-Frameworks wie Dagger/Hilt relativ einfach zu erlernen und zu verwenden.

*   **Negative:**
    *   **Laufzeit-Fehler:** Koin löst Abhängigkeiten zur Laufzeit auf, was bedeutet, dass Fehler in der DI-Konfiguration erst beim Start der Anwendung oder bei der Verwendung einer Abhängigkeit auftreten (im Gegensatz zu Compile-Zeit-Fehlern bei Dagger/Hilt).
    *   **Service Locator Pattern:** Koin wird manchmal als Service Locator kritisiert, was zu einer weniger strikten Einhaltung von DI-Prinzipien führen kann. Wir werden durch Code-Reviews darauf achten, dass Koin korrekt verwendet wird.

## Betrachtete Alternativen

*   **Dagger/Hilt:** Dagger und Hilt sind mächtige DI-Frameworks, die Compile-Zeit-Sicherheit bieten. Allerdings ist ihre Konfiguration komplexer und ihre Unterstützung für KMP ist weniger ausgereift als die von Koin.
*   **Manuelle DI:** Die manuelle Implementierung von Dependency Injection wäre eine Option gewesen, hätte aber zu einem erheblichen Mehraufwand und zu mehr Boilerplate-Code geführt.

## Referenzen

*   [Koin Dokumentation](https.koin.io)
*   [Repository-Architektur (MP-22)](docs/01_Architecture/ARCHITECTURE.md) (veraltet, aber erwähnt die DI-Policy)
