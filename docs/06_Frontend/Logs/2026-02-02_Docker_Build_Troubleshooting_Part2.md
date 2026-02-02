# 🧹 Troubleshooting Log: Gradle 9.x & KMP Docker Build (Part 2)

**Datum:** 02.02.2026
**Status:** ⚠️ BLOCKED (Docker Build Failure) / ✅ SUCCESS (Local Build)
**Thema:** `IsolatedKotlinClasspathClassCastException` im Docker-Build mit Gradle 9.3.1.

## 1. Zusammenfassung
Wir haben versucht, den Frontend-Build im Docker-Container zu stabilisieren. Lokal läuft der Build (`./gradlew build`) erfolgreich durch, inklusive WASM-Support und Runtime-Konfiguration. Im Docker-Container scheitert der Build jedoch hartnäckig an einem Plugin-Konflikt.

## 2. Das Problem
**Fehler:** `IsolatedKotlinClasspathClassCastException: The Kotlin Gradle plugin was loaded multiple times in different subprojects...`
**Kontext:** Gradle 9.2.1 / 9.3.1, Kotlin 2.3.0, Docker (ursprünglich `--no-daemon`).

### Analyse
*   Der Fehler tritt auf, weil das `NodeJsRootPlugin` (transitiv via KMP) mehrfach initialisiert wird.
*   **Lokal:** Der Gradle Daemon cached Classloader, wodurch das Plugin als "dasselbe" erkannt wird.
*   **Docker:** Durch die Isolation (und vermutlich Caching-Artefakte) werden Plugin-Klassen mehrfach geladen und sind nicht cast-bar (`ClassCastException`).

## 3. Durchgeführte Maßnahmen & Ergebnisse

| Versuch | Maßnahme | Ergebnis (Docker) | Erkenntnis |
| :--- | :--- | :--- | :--- |
| **1. Root Force** | `apply<NodeJsRootPlugin>()` im Root `build.gradle.kts`. | ❌ FAILED | Timing-Problem im Docker, Plugin wird zu spät oder falsch geladen. |
| **2. KMP Root** | `alias(...) apply true` im Root + `kotlin { jvm() }`. | ❌ FAILED | `IsolatedKotlinClasspathClassCastException` bleibt. |
| **3. Central Mgmt** | `pluginManagement` in `settings.gradle.kts` + `id("...")` ohne Version in Subprojekten. | ❌ FAILED | Architektonisch sauberster Weg, aber löst das Classloader-Problem im Docker nicht. |
| **4. Daemon** | Entfernen von `--no-daemon` im Dockerfile. | ❌ FAILED | Daemon startet, aber der Fehler tritt trotzdem auf. |
| **5. Upgrade** | Upgrade auf Gradle 9.3.1. | ❌ FAILED | Fehler persistiert auch in der neuesten Version. |
| **6. Property** | `kotlin.mpp.allowMultiplePluginDeclarations=true`. | ❌ FAILED | Scheint in Gradle 9.x / KMP 2.3.0 wirkungslos zu sein. |

## 4. Status Quo
*   **Lokal:** ✅ Build & Run funktionieren perfekt.
*   **Docker:** ❌ Build bricht ab.
*   **Architektur:** Wir haben jetzt eine sehr saubere Gradle-Konfiguration (Zentrales Plugin-Management), die wir beibehalten sollten.

## 5. Nächste Schritte (Hypothesen)
1.  **Cache Corruption:** Die Docker-Layer (`--mount=type=cache`) könnten korrupte Gradle-Caches enthalten. Ein Build *ohne* Cache-Mounts muss getestet werden.
2.  **Gradle 9 Inkompatibilität:** Es ist möglich, dass KMP 2.3.0 noch nicht vollständig kompatibel mit dem strikten Classpath-Isolation-Modus von Gradle 9 ist.
3.  **Workaround:** Ein Downgrade auf Gradle 8.x wurde diskutiert, aber abgelehnt. Wir müssen einen Weg finden, Gradle 9 zu zähmen.

---
*Dokumentiert durch Curator Agent.*
