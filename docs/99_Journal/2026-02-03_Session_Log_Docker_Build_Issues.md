# Session Log: Diagnose Docker Build Issues (IsolatedKotlinClasspathClassCastException)

**Datum:** 03.02.2026
**Teilnehmer:** User, DevOps Engineer
**Fokus:** Behebung des `IsolatedKotlinClasspathClassCastException` Fehlers im Docker-Build des Frontends.

## 🎯 Ziel
Den Docker-Build für den `web-app` Service reparieren, der mit einem `IsolatedKotlinClasspathClassCastException` fehlschlägt, während der lokale Build erfolgreich ist.

## 📝 Protokoll

### 1. Ausgangslage
*   **Fehler:** `IsolatedKotlinClasspathClassCastException: The Kotlin Gradle plugin was loaded multiple times in different subprojects...`
*   **Kontext:** Tritt nur im Docker-Container auf (Gradle 9.3.1, Java 25, KMP 2.3.0).
*   **Lokaler Build:** Funktioniert einwandfrei (`./gradlew clean build`).

### 2. Durchgeführte Maßnahmen & Analysen

#### A. Caching-Hypothese
*   **Vermutung:** Docker Build-Cache (`--mount=type=cache`) verursacht Inkonsistenzen.
*   **Aktion:** Cache-Mounts im Dockerfile deaktiviert.
*   **Ergebnis:** ❌ Build schlägt weiterhin fehl.

#### B. Plugin-Konfiguration (Subprojekte)
*   **Vermutung:** `frontend:core:auth` wendet Plugins falsch an oder hat Konflikte durch `browser()` Target.
*   **Aktion 1:** `browser()` durch `nodejs()` ersetzt. -> ❌ Fehlschlag.
*   **Aktion 2:** `js` Target komplett entfernt (temporär). -> ❌ Fehlschlag (Kompilierfehler, aber Plugin-Fehler weg -> Compose als Verdächtiger).
*   **Aktion 3:** Legacy `apply(plugin = ...)` Syntax versucht. -> ❌ Fehlschlag (DSL Accessors fehlen).

#### C. Root-Projekt Konfiguration
*   **Vermutung:** `NodeJsRootPlugin` wird nicht zentral geladen.
*   **Aktion 1:** `js { browser(); nodejs() }` im Root `build.gradle.kts` hinzugefügt. -> ❌ Fehlschlag.
*   **Aktion 2:** `apply<NodeJsRootPlugin>()` explizit im Root ausgeführt. -> ❌ Fehlschlag.
*   **Aktion 3:** `buildscript { dependencies { classpath("kotlin-gradle-plugin") } }` Hack angewendet, um Plugin in Root-Classloader zu zwingen. -> ❌ Fehlschlag.

#### D. Umgebungsvariablen & Daemon
*   **Vermutung:** Gradle Daemon oder parallele Ausführung im Docker verursacht Race Conditions.
*   **Aktion:** `--no-daemon` und `org.gradle.parallel=false` gesetzt. `GRADLE_USER_HOME` auf `/workspace/.gradle` geändert.
*   **Ergebnis:** ❌ Fehlschlag.

#### E. Downgrade-Versuch
*   **Aktion:** Versuch, auf Gradle 8.12 downzugraden.
*   **Ergebnis:** ❌ Gescheitert, da Gradle 8.12 Java 25 nicht unterstützt (`IllegalArgumentException: 25.0.2`).

### 3. Ergebnis & Fazit
*   Das Problem ist ein **Bug in der Interaktion zwischen Gradle 9.x, Kotlin 2.3.0 und Docker**.
*   Es manifestiert sich als Classloader-Isolation-Problem, bei dem das `NodeJsRootPlugin` mehrfach geladen wird, obwohl es das nicht sollte.
*   Alle gängigen Workarounds (Cache, Daemon, explizites Laden) haben versagt.
*   Ein Downgrade ist aufgrund der Java 25 Abhängigkeit nicht trivial möglich.

### 4. Nächste Schritte (Empfehlung)
1.  **Warten auf Upstream-Fix:** Das Problem liegt bei JetBrains/Gradle.
2.  **Temporärer Workaround (nicht implementiert):** Java-Version auf 21 senken und Gradle auf 8.10 downgraden (würde aber Code-Änderungen erfordern, da Java 25 Features genutzt werden).
3.  **Lokal bauen, Docker nur für Runtime:** Als Übergangslösung könnte das Artefakt lokal gebaut und in den Container kopiert werden, statt im Container zu bauen.

## 🧹 Cleanup
Alle experimentellen Änderungen an `build.gradle.kts`, `gradle.properties` und `Dockerfile` wurden rückgängig gemacht, um den Codebase sauber zu halten.
