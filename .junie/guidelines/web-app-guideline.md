# Client-App-Richtlinie (Compose Multiplatform)

## 1. Einleitung

Diese Richtlinie beschreibt die Architektur und die Best Practices für die Entwicklung der Client-Anwendungen für das "Meldestelle"-Projekt. Die Client-Anwendungen werden mit **Compose Multiplatform** für Desktop und Web entwickelt.

Das Hauptziel ist die maximale Wiederverwendung von Code zwischen den Desktop- und Web-Plattformen durch die konsequente Nutzung des `commonMain`-Source-Sets von Kotlin Multiplatform (KMP). Die Anwendung läuft sowohl als native Desktop-Anwendung (JVM) als auch als Web-Anwendung (WebAssembly).

## 2. Grundprinzipien

### Deklarative UI mit Composables

Die gesamte Benutzeroberfläche wird als Baum von `@Composable`-Funktionen deklariert. Dies ist derselbe Ansatz, der auch bei Jetpack Compose für Android verwendet wird.

- **Zustandslosigkeit:** Composables sollten bevorzugt zustandslos sein. Sie erhalten Daten als Parameter und geben Ereignisse über Lambda-Funktionen (Callbacks) nach oben weiter.
- **Wiederverwendbarkeit:** Erstellen Sie kleine, spezialisierte und wiederverwendbare Composables. Vermeiden Sie monolithische UI-Funktionen.
- **Vorschau:** Nutzen Sie `@Preview`-Annotationen (sofern von der IDE unterstützt), um UI-Komponenten isoliert zu entwickeln und zu visualisieren.

### State Management

Der UI-Zustand (State) wird explizit verwaltet.

- **`mutableStateOf` und `remember`**: Für einfachen, temporären UI-Zustand innerhalb einer Composable-Funktion.
- **State Hoisting**: Der Zustand sollte so weit wie möglich nach oben in der Komponentenhierarchie verschoben werden ("State Hoisting"), idealerweise in eine ViewModel- oder Presenter-Klasse in `commonMain`.
- **ViewModels/Presenters**: Komplexe Logik zur Zustandsverwaltung gehört in Klassen (z. B. `ExampleViewModel`) im `commonMain`-Modul. Diese Klassen sind plattformunabhängig und können von der UI (im `jsMain`-Modul) genutzt werden.

### Styling

Das Styling erfolgt plattformspezifisch, aber mit gemeinsamen Prinzipien:

#### Gemeinsame Styling-Prinzipien (commonMain)
- **Compose Material Design**: Nutzen Sie Material3-Komponenten und Theming für konsistente UI.
- **Gemeinsame Designsystem**: Definieren Sie gemeinsame Farben, Typografie und Spacing in `commonMain`.
- **Responsive Design**: Berücksichtigen Sie verschiedene Bildschirmgrößen (Desktop-Fenster vs. Browser-Viewports).

#### Web-spezifisches Styling (wasmJsMain)
- **CSS-Integration**: Web-spezifische Styling-Anforderungen können über CSS in den Resources behandelt werden.
- **Browser-Kompatibilität**: Berücksichtigen Sie Web-spezifische Rendering-Unterschiede.

#### Desktop-spezifisches Styling (jvmMain)
- **Native Look & Feel**: Desktop-Anwendungen sollten sich nativ anfühlen.
- **Fenster-Management**: Berücksichtigen Sie Desktop-spezifische UI-Patterns (Menüleisten, etc.).

```kotlin
// Beispiel für gemeinsames Theming in commonMain
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        typography = AppTypography,
        content = content
    )
}
```

### Navigation
Die Navigation wird plattformunabhängig in `commonMain` implementiert:
- **ViewModel-basierte Navigation**: Ein `StateFlow` oder `mutableState` im ViewModel repräsentiert die aktuelle Route/Screen.
- **Gemeinsamer Router**: Ein zentraler `Router`-Composable in `commonMain` reagiert auf Zustandsänderungen und rendert den entsprechenden Screen.
- **Plattformspezifische Einstiegspunkte**: Desktop und Web haben separate `main.kt`-Dateien, aber nutzen denselben gemeinsamen App-Composable.

## 3. Projekt- und Code-Struktur
Die Codebasis ist klar zwischen plattformunabhängiger Logik (`commonMain`) und plattformspezifischer Implementation (`jvmMain`, `wasmJsMain`) getrennt.

### Source Sets

- **`client/src/commonMain`**:
    - **UI-Code**: Alle `@Composable`-Funktionen, die zwischen Desktop und Web geteilt werden.
    - **Business-Logik**: ViewModels/Presenters, die den UI-Zustand verwalten.
    - **Data-Klassen**: Modelle, die Daten repräsentieren.
    - **Common Dependencies**: Shared Compose-Dependencies (runtime, foundation, material3, ui).

- **`client/src/jvmMain`** (Desktop-Plattform):
    - **`main.kt`**: Der Einstiegspunkt der Desktop-Anwendung.
    - **Desktop-spezifische Code**: Plattformspezifische Implementierungen und Integrationen.
    - **Desktop Dependencies**: `compose.desktop.currentOs`, Coroutines für Swing.

- **`client/src/wasmJsMain`** (Web-Plattform):
    - **`main.kt`**: Der Einstiegspunkt der Web-Anwendung (WebAssembly).
    - **Web-spezifische Code**: Browser-spezifische Implementierungen.
    - **Platform-spezifische Implementierungen**: Web-APIs und Browser-Integrationen.

- **`client/src/wasmJsMain/resources`**:
    - **`index.html`**: Das Host-HTML-Dokument für die Compose-Anwendung.
    - **Statische Assets**: Bilder, Schriftarten und andere statische Dateien für die Web-Version.

### Shared Module Integration
- **`core/commonMain`** (oder äquivalente `shared`-Module):
    - **Repositories/Services**: Code für den Datenzugriff (z.B. Ktor-HTTP-Clients zum Aufrufen des Backends).
    - **Business-Logik**: Plattformunabhängige Geschäftslogik, die von allen Client-Plattformen genutzt wird.

## 4. Entwicklung und Ausführung

### Desktop-Entwicklung
Für die Desktop-Anwendung stehen folgende Gradle-Tasks zur Verfügung:

```shell script
# Desktop-Anwendung direkt ausführen
./gradlew :client:run

# Desktop-Distribution erstellen (DMG, MSI, DEB)
./gradlew :client:createDistributable
./gradlew :client:packageDmg        # macOS
./gradlew :client:packageMsi        # Windows
./gradlew :client:packageDeb        # Linux
```

### Web-Entwicklung mit Hot-Reload
Für die Web-Anwendung mit automatischer Neuladung bei Änderungen:

```shell script
# Web-App mit Hot-Reload starten
./gradlew :client:wasmJsBrowserDevelopmentRun
```

#### Docker-Setup für Web-Entwicklung
Das Docker-Setup ist spezifisch für die Web-Entwicklung konfiguriert (wie in `README-DOCKER.md` beschrieben):

```shell script
# Startet die Web-App mit Hot-Reload
docker-compose -f docker-compose.yml \
-f docker-compose.clients.yml up -d web-app
```

Der Dienst ist dann unter dem in der `docker-compose.clients.yml` konfigurierten Port (z.B. Port `3000`) erreichbar.

### Produktions-Builds

#### Desktop-Distribution
```shell script
# Erstellt native Distributionen für alle konfigurierten Plattformen
./gradlew :client:packageDistributionForCurrentOS
```

#### Web-Distribution
```shell script
# Erstellt optimierte WebAssembly-Artefakte für die Produktion
./gradlew :client:wasmJsBrowserDistribution
```

Das Docker-Image für die Web-Produktion (`Dockerfile` im `client`-Verzeichnis) sollte den `wasmJsBrowserDistribution`-Task nutzen, um die finalen Artefakte zu bauen.
## 5. Plattformspezifische Besonderheiten

### Desktop (jvmMain)
- **Fenster-Management**: Nutzen Sie Compose Desktop-APIs für Fensteroperationen.
- **System-Integration**: Zugriff auf Desktop-spezifische Features (Dateisystem, Notifications, etc.).
- **Performance**: Desktop-Apps können mehr Ressourcen nutzen als Web-Apps.

### Web (wasmJsMain)
- **Browser-APIs**: Zugriff auf Web-APIs erfolgt über `external`-Deklarationen.
- **Bundle-Size**: Achten Sie auf die Größe der WebAssembly-Bundles für optimale Ladezeiten.
- **SEO und Accessibility**: Berücksichtigen Sie Web-spezifische Anforderungen.

## 6. Dos and Don'ts

### Multiplatform Best Practices
- **DO**: Die gesamte UI-Logik (State-Management, Datenabruf, Validierung) in `commonMain` implementieren.
- **DO**: Kleine, wiederverwendbare und zustandslose Composables in `commonMain` erstellen.
- **DO**: Material3 und gemeinsames Theming für konsistente UI zwischen Plattformen verwenden.
- **DO**: Events von der UI über Lambda-Funktionen an die ViewModels in `commonMain` weiterleiten.
- **DO**: Plattformspezifische Features über `expect`/`actual`-Mechanismus abstrahieren.

### Platform-Specific Guidelines
- **DO** (Desktop): Native Look & Feel und Desktop-UI-Patterns verwenden.
- **DO** (Web): Web-Standards und Accessibility-Guidelines befolgen.

### Don'ts
- **DON'T**: Geschäftslogik, API-Aufrufe oder komplexe Zustandsmanipulationen direkt in `@Composable`-Funktionen schreiben.
- **DON'T**: Plattformspezifische Code direkt in `commonMain` verwenden ohne `expect`/`actual`.
- **DON'T** (Web): Den DOM direkt manipulieren. Compose Multiplatform verwaltet das Rendering. Falls Interaktion mit externen Bibliotheken nötig ist, nutzen Sie `external`-Mechanismen sauber gekapselt.
- **DON'T**: Annahmen über die Zielplattform in `commonMain` machen.

---
_Letzte Aktualisierung: 2025-01-10_

Diese Richtlinie bietet eine solide Grundlage für die Entwicklung Ihrer Desktop- und Web-Anwendungen mit Compose Multiplatform und stellt sicher, dass neue Teammitglieder die Multiplatform-Architektur und die erwarteten Konventionen schnell verstehen.

