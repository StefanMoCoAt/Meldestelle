# Web-App-Richtlinie (Compose for Web)

## 1. Einleitung

Diese Richtlinie beschreibt die Architektur und die Best Practices für die Entwicklung des Web-Frontends für das "Meldestelle"-Projekt. Das Frontend wird mit **Compose for Web** (Teil von Compose Multiplatform) entwickelt.

Das Hauptziel ist die maximale Wiederverwendung von Code zwischen verschiedenen Plattformen (potenziell Web, Android, Desktop) durch die konsequente Nutzung des `commonMain`-Source-Sets von Kotlin Multiplatform (KMP).

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

Das Styling erfolgt über eine Kotlin-DSL, die CSS abbildet.

- **`StyleSheet`**: Definieren Sie Stile in einem `object`, das von `StyleSheet` erbt. Dies fördert die Wiederverwendbarkeit und Typsicherheit.
- **Scoped Styles**: Organisieren Sie Stile logisch, z. B. pro Komponente oder pro Bildschirm.
- **Kein globales CSS**: Vermeiden Sie die Verwendung globaler, ungebundener CSS-Dateien. Das Styling sollte ausschließlich über die Kotlin-DSL verwaltet werden, um Konflikte zu vermeiden und die Kapselung zu wahren.

```kotlin
// Beispiel für ein StyleSheet
object AppStylesheet : StyleSheet() {
    val container by style {
        padding(24.px)
        border(1.px, LineStyle.Solid, Color.black)
    }
}
```

### Navigation
Die Navigation zwischen verschiedenen Bildschirmen ("Screens") wird durch Zustandsänderungen gesteuert. Da es noch keine offizielle "Compose Navigation"-Bibliothek für das Web gibt, verwenden wir einen einfachen, zustandsbasierten Ansatz.
- Ein `StateFlow` oder eine `mutableState`-Variable im ViewModel kann die aktuell anzuzeigende Route/Screen repräsentieren.
- Ein zentraler -Composable kann auf Änderungen dieses Zustands reagieren und den entsprechenden Screen rendern. `Router`

## 3. Projekt- und Code-Struktur
Die Codebasis ist klar zwischen plattformunabhängiger Logik (`commonMain`) und plattformspezifischer UI (`jsMain`) getrennt.
- **`core/commonMain`** (oder ein äquivalentes `shared`-Modul):
    - **Business-Logik**: Die gesamte Geschäftslogik.
    - **ViewModels/Presenters**: Klassen, die den UI-Zustand verwalten und auf Benutzerinteraktionen reagieren.
    - **Data-Klassen**: Modelle, die Daten repräsentieren.
    - **Repositories/Services**: Code für den Datenzugriff (z.B. Ktor-HTTP-Clients zum Aufrufen des Backends).

- **`client/jsMain`**:
    - **UI-Code**: Ausschließlich `@Composable`-Funktionen.
    - **`main.kt`**: Der Einstiegspunkt der Web-Anwendung.
    - **`StyleSheet.kt`**: Die Kotlin-CSS-DSL-Definitionen.
    - **Abhängigkeit von `commonMain`**: Die UI konsumiert die ViewModels und Datenklassen aus `commonMain`. Die UI ist "dumm" und dient nur zur Darstellung des Zustands.

- **`client/src/jsMain/resources`**:
    - **`index.html`**: Das Host-HTML-Dokument für die Compose-Anwendung.
    - **Statische Assets**: Bilder, Schriftarten und andere statische Dateien.

## 4. Entwicklung und Ausführung
### Lokale Entwicklung mit Hot-Reload
Für eine schnelle Entwicklungs-Loop mit automatischer Neuladung bei Änderungen wird der Gradle-Task `jsBrowserDevelopmentRun` verwendet. Dies wird durch unser Docker-Setup vereinfacht.
Um die Web-App im Entwicklungsmodus zu starten (wie in beschrieben): `README-DOCKER.md`

```shell script
# Startet die Web-App mit Hot-Reload
docker-compose -f docker-compose.yml \
-f docker-compose.clients.yml up -d web-app
```

Der Dienst ist dann unter dem in der `docker-compose.clients.yml` konfigurierten Port (z.B. Port `3000`) erreichbar.
### Produktions-Build
Um eine optimierte JavaScript-Datei für die Produktion zu erstellen, wird der Gradle-Task `jsBrowserDistribution` verwendet. Das Docker-Image für die Produktion ( im `client`-Verzeichnis) sollte diesen Task nutzen, um die finalen Artefakte zu bauen. `Dockerfile`
## 5. Dos and Don'ts
- **DO**: Die gesamte Logik (State-Management, Datenabruf, Validierung) in `commonMain` implementieren.
- **DO**: Kleine, wiederverwendbare und zustandslose Composables erstellen.
- **DO**: Styling ausschließlich über die Kotlin-CSS-DSL (`StyleSheet`) realisieren.
- **DO**: Events von der UI über Lambda-Funktionen an die ViewModels in `commonMain` weiterleiten.
- **DON'T**: Geschäftslogik, API-Aufrufe oder komplexe Zustandsmanipulationen direkt in `@Composable`-Funktionen schreiben.
- **DON'T**: Den DOM direkt manipulieren. Compose for Web verwaltet den DOM. Falls eine Interaktion mit externen JS-Bibliotheken unumgänglich ist, nutzen Sie die `external`- und `@JsModule`-Mechanismen von Kotlin/JS sauber gekapselt.
- **DON'T**: Globale CSS-Dateien verwenden, die mit der von Compose generierten Stil-Logik in Konflikt geraten könnten.
---
_Letzte Aktualisierung: 2025-09-10_
Diese Richtlinie bietet eine solide Grundlage für die Entwicklung Ihrer Webanwendung mit Compose for Web und stellt sicher, dass neue Teammitglieder die Architektur und die erwarteten Konventionen schnell verstehen.

