# Platform Module

## Überblick

Das **Platform-Modul** ist das Rückgrat der Build-Infrastruktur des Meldestelle-Projekts. Seine alleinige Aufgabe ist die zentrale Verwaltung und Bereitstellung von Abhängigkeiten und deren Versionen. Dies stellt sicher, dass alle Module im gesamten Projekt dieselben Bibliotheksversionen verwenden, was Inkonsistenzen ("JAR Hell") verhindert und die Wartbarkeit drastisch verbessert.

Das Modul agiert als eine interne "Single Source of Truth" für alle externen Bibliotheken.

## Architektur

Das Platform-Modul ist in drei spezialisierte Untermodule aufgeteilt, die jeweils eine klare Aufgabe haben:


platform/
├── platform-bom/                 # Bill of Materials (BOM) - Erzwingt Versionen
├── platform-dependencies/        # Bündelt gemeinsame Laufzeit-Abhängigkeiten
└── platform-testing/             # Bündelt gemeinsame Test-Abhängigkeiten


### `platform-bom`

Dies ist das wichtigste Modul der Plattform. Es ist als "Bill of Materials" (BOM) konfiguriert und nutzt das `java-platform`-Plugin von Gradle.

* **Zweck:** Definiert eine umfassende Liste von Abhängigkeiten und deren exakten, geprüften Versionen. Es importiert auch andere wichtige BOMs (z.B. von Spring Boot und Kotlin).
* **Funktionsweise:** Andere Module importieren diese BOM mit `platform(projects.platform.platformBom)`. Gradle sorgt dann dafür, dass alle transitiven und deklarierten Abhängigkeiten den in der BOM festgelegten Versionen entsprechen.
* **Vorteil:** Absolute Versionskontrolle über das gesamte Projekt.

### `platform-dependencies`

Ein einfaches "Sammelmodul", das die am häufigsten benötigten Laufzeit-Abhängigkeiten bündelt.

* **Zweck:** Vereinfacht die `build.gradle.kts`-Dateien der Service-Module. Anstatt 5-6 einzelne `kotlinx`- und Logging-Bibliotheken hinzuzufügen, genügt eine einzige Abhängigkeit zu diesem Modul.
* **Verwendung:**
    ```kotlin
    // In einem Service-Modul
    dependencies {
        implementation(projects.platform.platformDependencies)
    }
    ```

### `platform-testing`

Analog zu `platform-dependencies`, aber speziell für Test-Bibliotheken.

* **Zweck:** Stellt ein konsistentes Set an Test-Frameworks (JUnit 5, MockK, AssertJ) und Werkzeugen (Testcontainers) für alle Module bereit.
* **Verwendung:**
    ```kotlin
    // In einem Service-Modul
    dependencies {
        testImplementation(projects.platform.platformTesting)
    }
    ```
* **Optimierung:** Dieses Modul nutzt die in `libs.versions.toml` definierten `[bundles]`, um die Build-Datei extrem kurz und lesbar zu halten.

---
**Letzte Aktualisierung**: 31. Juli 2025
