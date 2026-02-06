---
type: Journal
status: COMPLETED
owner: Curator
date: 2026-01-29
participants:
  - Lead Architect
  - DevOps Engineer
  - Curator
---

# Session Log: 29. Jänner 2026 - Etablierung robuster Architektur-Guards mit ArchUnit

## Zielsetzung
Etablierung eines robusten, wartbaren und zentralisierten Systems zur Überprüfung und Erzwingung der Projekt-Architekturregeln, insbesondere der Trennung zwischen Modulen (Features, Services).

## Durchgeführte Arbeiten

### 1. Strategische Entscheidung
*   **Problem:** Die Notwendigkeit, die modulare Trennung (z.B. zwischen Frontend-Features und Backend-Services) technisch zu erzwingen, wurde als kritisch für die Langlebigkeit des Projekts identifiziert.
*   **Analyse:** Die bestehenden, skriptbasierten Gradle-Tasks (`archGuard...`) wurden als clever, aber fragil und nicht universell genug bewertet.
*   **Entscheidung:** Es wurde beschlossen, auf **ArchUnit** als zentrales, typsicheres Werkzeug für die Architektur-Verifizierung zu migrieren.

### 2. Implementierung (Iterativer Prozess & Fehlerbehebung)
Die Implementierung war ein mehrstufiger Prozess, der mehrere Herausforderungen aufdeckte:

1.  **Modul-Erstellung:** Ein neues Modul `:platform:architecture-tests` wurde erstellt und die ArchUnit-Abhängigkeiten (Version `1.4.1`) wurden im zentralen Versionskatalog `gradle/libs.versions.toml` deklariert.

2.  **Abhängigkeits-Problem:** Der erste Versuch, die Abhängigkeiten dynamisch zu sammeln (`rootProject.subprojects.filter`), schlug fehl, da er versuchte, nicht-baubare Verzeichnis-Module (z.B. `:backend`) einzubinden.
    *   **Lösung:** Umstellung auf eine explizite, manuelle Liste von `implementation(project(":..."))`-Abhängigkeiten in der `build.gradle.kts` des Test-Moduls.

3.  **Klassen-Auffindungs-Problem:** Die ersten Test-Implementierungen schlugen mit der Meldung `Rule '...' failed to check any classes` fehl. Dies lag an einer Kombination aus falschen Paket-Mustern und einer inkorrekten Konfiguration des ArchUnit-Klassen-Imports.
    *   **Lösung:** Die finale, robuste Lösung besteht darin, die `@AnalyzeClasses`-Annotation in den Testklassen zu verwenden und den `packages`-Parameter auf das Root-Package (`at.mocode`) zu setzen. Dies stellt sicher, dass ArchUnit den gesamten relevanten Classpath scannt, der von Gradle durch die expliziten Abhängigkeiten bereitgestellt wird.

4.  **Syntax-Korrekturen:** Mehrere Iterationen waren nötig, um die korrekte Syntax für die ArchUnit-Regeln (insbesondere `slices().matching(...)` und die Paket-Identifier) zu finden.

### 3. Finale Konfiguration

*   **`build.gradle.kts` (`:platform:architecture-tests`):** Enthält eine explizite, manuell gepflegte Liste von Abhängigkeiten zu allen Modulen, die Code enthalten.
*   **`FrontendArchitectureTest.kt`:** Verwendet `@AnalyzeClasses(packages = ["at.mocode.frontend"])` und die Regel `slices().matching("..frontend.features.(*)..").should().notDependOnEachOther()`.
*   **`BackendArchitectureTest.kt`:** Verwendet `@AnalyzeClasses(packages = ["at.mocode.backend"])` und eine explizite `noClasses()..`-Regel, da die Backend-Services keine einheitliche Paketstruktur haben.
*   **`README.md`:** Eine neue `README.md` wurde im Modul erstellt, um dessen Zweck und Verwendung zu dokumentieren.

## Ergebnis & Status
*   **BUILD SUCCESSFUL:** Die Architektur-Tests kompilieren und laufen erfolgreich durch.
*   **Keine Verletzungen gefunden:** Die bestehende Codebasis ist sauber und verstößt nicht gegen die neu aufgestellten Regeln.
*   Das Projekt verfügt nun über ein zentrales, robustes und erweiterbares System zur Durchsetzung von Architektur-Regeln, das in der CI-Pipeline ausgeführt wird.
*   Die alten `archGuard`-Tasks wurden aus der Root-Build-Datei entfernt.
