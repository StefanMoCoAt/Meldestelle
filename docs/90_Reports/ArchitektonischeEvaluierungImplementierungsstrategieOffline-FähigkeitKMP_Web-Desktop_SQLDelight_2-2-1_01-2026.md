# Architektonische Evaluierung und Implementierungsstrategie für echte Offline-Fähigkeit in Kotlin Multiplatform (Web/Desktop) unter Verwendung von SQLDelight 2.2.1

## 1. Executive Summary und Kompatibilitätsanalyse

### 1.1 Einführung und Zielsetzung
Dieser Bericht bietet eine umfassende, technisch detaillierte Analyse zur Realisierung einer „echten“ Offline-First-Architektur innerhalb einer Kotlin Multiplatform (KMP) Umgebung. Der Fokus liegt auf der nahtlosen Integration der Zielplattformen Web (JS/Wasm) und Desktop (JVM) unter Nutzung einer gemeinsamen Codebasis für die Datenpersistenz. Die Anforderung spezifiziert einen hochmodernen Technologie-Stack, bestehend aus **Kotlin 2.3.0**, **Java 25**, **Compose Multiplatform 1.10.0-rc02** und **SQLDelight 2.2.1**.

Die zentrale Herausforderung dieses Vorhabens liegt in der fundamentalen Diskrepanz der E/A-Modelle (Ein-/Ausgabe) der beteiligten Plattformen: Während moderne Desktop-Umgebungen auf der JVM (Java Virtual Machine) traditionell effiziente blockierende E/A-Operationen – zunehmend optimiert durch virtuelle Threads – unterstützen, erzwingt die Browser-Umgebung für persistente Speicheroperationen (insbesondere über das Origin Private File System, OPFS) eine strikte Asynchronität, um den Haupt-Thread nicht zu blockieren.

Die Analyse bestätigt, dass die gewählte Kombination von Versionen nicht nur kompatibel ist, sondern eine synergetische Wirkung entfaltet, die erst durch die jüngsten Fortschritte im Kotlin- und Java-Ökosystem möglich wurde. Insbesondere die Stabilisierung von Kotlin/Wasm und die Einführung asynchroner Treiber-Schnittstellen in SQLDelight 2.x sind Schlüsselfaktoren für den Erfolg dieser Architektur.

### 1.2 Detaillierte Versions-Kompatibilitätsmatrix
Die folgende Tabelle schlüsselt die Interoperabilität der spezifizierten Komponenten auf und bewertet deren Bereitschaft für den Produktionseinsatz in einem Offline-Szenario.

| Komponente | Version | Status (Stand Anfang 2026) | Rolle & Kompatibilitätsbewertung |
| :--- | :--- | :--- | :--- |
| **Kotlin** | **2.3.0** | Stable (Dez 2025) | Fungiert als das fundamentale Bindeglied. Version 2.3.0 bringt entscheidende Stabilisierungen für Kotlin/Wasm, einschließlich standardmäßig aktivierter vollqualifizierter Namen (Fully Qualified Names), was für die Reflection-freie Serialisierung und Datenbank-Mapping im Web essentiell ist.[1, 2] Die Unterstützung für Java 25 Bytecode ist vollständig implementiert.[3] |
| **Java** | **25** | LTS (Sep 2025) | Dient als Laufzeitumgebung für den Desktop-Client. Java 25 ist ein Long-Term-Support Release, das Features wie "Flexible Constructor Bodies" und stabilisierte "Scoped Values" bietet.[4, 5] Diese Features harmonieren exzellent mit Kotlin Coroutines, insbesondere bei der Verwaltung von Transaktionskontexten auf dem Desktop. |
| **Compose Multiplatform** | **1.10.0-rc02** | Release Candidate | Stellt die UI-Schicht bereit. Diese Version behebt kritische Fehler beim Laden von Ressourcen, die in Beta-Versionen auftraten, und vereinheitlicht die `@Preview` Annotationen, was den Entwicklungsprozess beschleunigt.[6, 7, 8] Die Abhängigkeit von Kotlin 2.2+ gewährleistet volle Kompatibilität mit Kotlin 2.3.0. |
| **SQLDelight** | **2.2.1** | Stable (Nov 2025) | Das Herzstück der Persistenz. Version 2.2.1 adressiert spezifische Wasm-Kompatibilitätsprobleme und stabilisiert die asynchronen Schnittstellen (`WebWorkerDriver`), die für die OPFS-Integration zwingend erforderlich sind.[9, 10] |

### 1.3 Das Paradigma der "Echten Offline-Fähigkeit" im Web
Traditionelle Ansätze für SQLite im Browser basierten auf `sql.js` (asm.js oder Wasm), welches die Datenbank vollständig im Arbeitsspeicher (RAM) hält. Persistenz wurde durch den Export des gesamten Byte-Arrays in den `localStorage` oder `IndexedDB` simuliert. Dieser Ansatz ist für ernsthafte Anwendungen ungeeignet, da er bei Datenbankgrößen über 5-10 MB massive Performance-Einbußen verursacht und das Risiko von Datenverlust bei Abstürzen birgt.[11]

"Echte Offline-Fähigkeit" definiert sich in diesem Kontext durch die Nutzung des **Origin Private File System (OPFS)**. OPFS ermöglicht performanten, wahlfreien Zugriff (Random Access) auf Dateien direkt im Browser, ähnlich einem nativen Dateisystem. Dies erlaubt SQLite, nur die benötigten "Pages" (Seiten) der Datenbankdatei zu lesen oder zu schreiben, anstatt die gesamte Datei zu laden.

**Kritische technische Einschränkung:** Die synchronen Zugriffshandles (`FileSystemSyncAccessHandle`), die SQLite für die notwendige Performance benötigt, sind im Haupt-Thread des Browsers (UI Thread) **verboten**.[12, 13] Dies erzwingt eine Architektur, bei der die Datenbankinteraktion in einen **Web Worker** ausgelagert werden muss. SQLDelight muss daher so konfiguriert werden, dass es asynchronen Code generiert, um die Kommunikation zwischen Haupt-Thread und Worker (via `postMessage`) abzubilden.[14]

---

## 2. Technologischer Kontext und Ökosystem-Analyse

Um die Tragweite der Architekturentscheidung zu verstehen, ist eine tiefergehende Analyse der Einzelkomponenten im Kontext von 2025/2026 notwendig.

### 2.1 Kotlin 2.3.0: Die Ära der Stabilität
Kotlin 2.3.0, veröffentlicht im Dezember 2025, markiert einen Wendepunkt für die Multiplattform-Entwicklung.
*   **Sprach-Features:** Die Einführung von expliziten "Backing Fields" vereinfacht die Zustandsverwaltung in ViewModels, was direkt in die UI-Logik von Compose einfließt.[1] Der "Unused Return Value Checker" erhöht die Code-Qualität, insbesondere bei Fluent-APIs wie SQL-Query-Buildern.[3]
*   **UUIDv7 Support:** Die Standardbibliothek unterstützt nun UUIDv7 (zeitbasierte UUIDs). Dies ist für verteilte Datenbanken von immenser Bedeutung, da UUIDv7-Schlüssel in B-Tree-Indizes (wie sie SQLite verwendet) eine deutlich bessere Lokalität aufweisen als zufällige UUIDv4, was die Insert-Performance bei großen Offline-Datensätzen drastisch verbessert.[1]
*   **Wasm-Reife:** Die vollständige Unterstützung für qualifizierte Namen im Wasm-Target eliminiert frühere Probleme bei der Nutzung von Reflection-ähnlichen Mechanismen, die oft von Serialisierungs-Bibliotheken verwendet werden, um Daten zwischen DB und UI zu mappen.[2]

### 2.2 Java 25: Performance-Fundament für den Desktop
Obwohl der Desktop-Client in Kotlin geschrieben ist, profitiert er massiv von der zugrundeliegenden JVM-Version. Java 25 (LTS) bringt "Compact Object Headers" (JEP 519) standardmäßig mit.[5]
*   **Auswirkung:** In einer datenintensiven Anwendung, die Tausende von Zeilen aus einer SQLite-Datenbank in Kotlin-Datenklassen mappt, reduziert sich der Overhead pro Objekt signifikant. Dies führt zu geringerem Speicherdruck und selteneren Garbage-Collection-Pausen, was für eine flüssige 120Hz-UI in Compose Desktop essenziell ist.
*   **Scoped Values (JEP 506):** Diese bieten eine effiziente Alternative zu `ThreadLocal`. In Verbindung mit Kotlin Coroutines (die auf Java Virtual Threads gemappt werden können) ermöglicht dies eine extrem skalierbare Handhabung von Datenbankverbindungen, falls der Desktop-Client auch als Server-Komponente agieren sollte.[5]

### 2.3 SQLDelight 2.2.1: Der Asynchrone Wandel
SQLDelight hat mit der Version 2.x einen Paradigmenwechsel vollzogen. Frühere Versionen waren primär synchron. Die Version 2.2.1 verfeinert das Modell für asynchrone Treiber (`generateAsync = true`).
*   **Treiber-Architektur:** Es wird strikt zwischen `JdbcSqliteDriver` (Synchron, JVM) und `WebWorkerDriver` (Asynchron, JS/Wasm) unterschieden.
*   **Kompatibilität:** Version 2.2.1 behebt spezifische Linker-Probleme, die bei der Nutzung von Wasm-Targets in früheren 2.x-Versionen auftraten, und stellt sicher, dass die generierten Interfaces korrekt mit den Coroutine-Scopes interagieren.[9, 15]

---

## 3. Architektur-Design für "True Offline" Persistenz

Die Realisierung echter Offline-Fähigkeit erfordert ein Architekturmuster, das die synchrone Natur von SQLite (auf dem Desktop) und die erzwungene Asynchronität des Browsers (OPFS) abstrahiert.

### 3.1 Das "Async-First" Prinzip im Shared Core

Um Code-Duplizierung zu vermeiden, muss der "kleinste gemeinsame Nenner" das Design bestimmen. Da der Web-Treiber *zwingend* asynchron ist (suspend functions), müssen die Schnittstellen im gemeinsamen Modul (`commonMain`) ebenfalls asynchron definiert werden, selbst wenn die Desktop-Implementierung diese synchron ausführt.

**Konfigurations-Implikation:**
In der `build.gradle.kts` des Shared-Moduls muss die Einstellung `generateAsync.set(true)` aktiviert werden.[14, 16]

```kotlin
// build.gradle.kts (Ausschnitt)
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.persistence")
            generateAsync.set(true) // Erzwingt suspend Functions in generierten Queries
            verifyMigrations.set(true)
        }
    }
}
```

### 3.2 Architektur-Diagramm (Konzeptionell)

Die Datenfluss-Architektur stellt sich wie folgt dar:

1.  **UI Layer (Compose):** Ruft Daten über `ViewModel` ab. Beobachtet `Flow<T>`.
2.  **Domain Layer (Repository):** Exponiert `suspend` Funktionen für Writes und `Flow` für Reads.
3.  **Data Layer (SQLDelight Interface):**
  *   *Interface:* `suspend fun selectAll(): List<Task>`
4.  **Driver Layer (Platform Specific):**
  *   *JVM:* `JdbcSqliteDriver` (Blockiert den Thread, muss auf `Dispatchers.IO` gewrappt werden).
  *   *Web:* `WebWorkerDriver` -> `postMessage` -> `Worker` -> `sqlite3.wasm` -> `OPFS`.

### 3.3 Die Rolle des Web Workers und OPFS

Der Web Worker fungiert als dedizierter Datenbank-Server innerhalb des Browsers.
*   **Haupt-Thread:** Sendet SQL-Strings oder präparierte Statements an den Worker.
*   **Worker-Thread:**
  1.  Empfängt Nachricht.
  2.  Nutzt `sqlite3-wasm`.
  3.  Öffnet Datenbankdatei via OPFS (`opfs-sahpool` VFS).
  4.  Führt Query synchron (!) innerhalb des Worker-Threads aus.
  5.  Sendet Ergebnis zurück an Haupt-Thread.

Dieses Design ist entscheidend, da OPFS `createSyncAccessHandle` nur im Worker erlaubt ist. Würde man versuchen, dies im Haupt-Thread zu tun, würde der Browser eine Exception werfen.[12, 13]

---

## 4. Implementierungsstrategie: Web (Wasm/JS)

Dies ist der komplexeste Teil der Implementierung. Die Standard-Dokumentation deckt oft nur einfache In-Memory-Beispiele ab. Für "True Offline" müssen wir tiefer gehen.

### 4.1 Webpack und Header-Konfiguration
Damit OPFS und `SharedArrayBuffer` funktionieren, muss der Server (auch der Dev-Server) spezifische HTTP-Header senden. Ohne diese Header isoliert der Browser den Prozess nicht genügend, um die sicherheitskritischen Features wie `SharedArrayBuffer` freizuschalten, auf denen SQLite Wasm basiert.[13, 17]

Erstellen Sie eine Datei `webpack.config.d/sqljs.js`:

```javascript
// webpack.config.d/sqljs.js
const CopyWebpackPlugin = require('copy-webpack-plugin');

config.plugins.push(
    new CopyWebpackPlugin({
        patterns:
    })
);

// Essenzielle Header für OPFS Support
config.devServer = config.devServer |

| {};
config.devServer.headers = {
  ...config.devServer.headers,
    "Cross-Origin-Opener-Policy": "same-origin",
    "Cross-Origin-Embedder-Policy": "require-corp"
};
```

### 4.2 Der Custom Worker (Die Brücke zu OPFS)
Der Standard-Worker von SQLDelight (`@cashapp/sqldelight-sqljs-worker`) ist oft auf `sql.js` (Memory-only) ausgelegt. Für OPFS müssen wir einen eigenen Worker-Einstiegspunkt definieren oder den bestehenden konfigurieren, um das OPFS VFS zu laden.

In `src/wasmJsMain/resources/sqlite.worker.js` (oder ähnlich):

```javascript
import { runWorker } from '@cashapp/sqldelight-sqljs-worker';
// Importieren Sie die offizielle SQLite Wasm Implementierung, die OPFS unterstützt
import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

sqlite3InitModule({
    print: console.log,
    printErr: console.error,
}).then((sqlite3) => {
    // Prüfen, ob OPFS verfügbar ist
    const opfsAvailable = 'opfs' in sqlite3;
    
    runWorker({
        driver: {
            open: (name) => {
                if (opfsAvailable) {
                    // Nutzung des OPFS Backend
                    console.log("Initialisiere persistente OPFS Datenbank");
                    return new sqlite3.oo1.OpfsDb(name);
                } else {
                    console.warn("OPFS nicht verfügbar, Fallback auf In-Memory");
                    return new sqlite3.oo1.DB(name);
                }
            }
        }
    });
});
```
*Anmerkung:* Dieser Code ist konzeptionell. Die genaue API von `@cashapp/sqldelight-sqljs-worker` erlaubt möglicherweise das direkte Übergeben einer Treiber-Instanz oder erfordert Anpassungen, je nachdem wie stark die API in Version 2.2.1 gekapselt ist. Das Kernprinzip bleibt: Der Worker muss `sqlite3.oo1.OpfsDb` instanziieren anstelle der Standard `DB` Klasse.[18, 19]

### 4.3 Initialisierung des Treibers in Kotlin

In `src/wasmJsMain/kotlin/DatabaseDriverFactory.kt`:

```kotlin
actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        // Der Worker muss als URL geladen werden, damit Webpack ihn separat bündeln kann
        val worker = Worker(
            js("""new URL("sqlite.worker.js", import.meta.url)""")
        )
        // WebWorkerDriver ist die asynchrone Brücke
        val driver = WebWorkerDriver(worker)
        
        // WICHTIG: Schema-Erstellung muss explizit und asynchron erfolgen
        // und erfordert.await() da generateAsync=true
        AppDatabase.Schema.create(driver).await()
        
        return driver
    }
}
```

---

## 5. Implementierungsstrategie: Desktop (JVM/Java 25)

Der Desktop-Teil ist scheinbar einfacher, birgt aber eine Falle: Die Diskrepanz zwischen der asynchronen API (durch `generateAsync=true`) und dem synchronen JDBC-Treiber.

### 5.1 Der Synchron-zu-Asynchron Adapter
Der `JdbcSqliteDriver` implementiert das `SqlDriver` Interface. Wenn `generateAsync=true` gesetzt ist, erwartet der generierte Code jedoch Methoden, die `QueryResult.AsyncValue` zurückgeben oder suspendieren.

Glücklicherweise bietet SQLDelight Erweiterungsfunktionen oder Adapter, um dies zu handhaben, aber oft ist der sauberste Weg, die Asynchronität im Aufrufer (Repository) zu managen. Da `generateAsync=true` die Schnittstelle der generierten *Queries* ändert (sie werden zu `suspend` Funktionen), muss der Treiber dies unterstützen.

Für die JVM bedeutet dies: Obwohl die Funktionssignatur `suspend` ist, wird der Code darin blockierend ausgeführt.

### 5.2 Java 25 Optimierungen (Virtual Threads)
Hier kommt Java 25 ins Spiel. Wir können den `JdbcSqliteDriver` in einem Kontext ausführen, der virtuelle Threads nutzt.

In `src/desktopMain/kotlin/DatabaseDriverFactory.kt`:

```kotlin
actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:app_database.db")
        
        // Migration und Erstellung müssen ebenfalls asynchron behandelt werden (von der Signatur her)
        //.await() ist hier notwendig, um das QueryResult aufzulösen, auch wenn es synchron fertig ist.
        AppDatabase.Schema.create(driver).await()
        
        return driver
    }
}
```

**Nutzung von Virtual Threads:**
Anstatt den Standard `Dispatchers.IO` zu verwenden (der auf einem Thread-Pool basiert), können wir in Java 25 einen Executor-Service auf Basis von Virtual Threads erstellen und diesen als Coroutine Dispatcher nutzen.

```kotlin
// Java 25 Virtual Thread Dispatcher
val VirtualThreadDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

// Im Repository
suspend fun insertTask(task: Task) = withContext(VirtualThreadDispatcher) {
    // Dieser blockierende JDBC-Aufruf "parkt" nun den virtuellen Thread
    // anstatt den OS-Thread zu blockieren. Massive Skalierbarkeit!
    db.taskQueries.insert(task)
}
```
Dies ist eine signifikante architektonische Verbesserung gegenüber älteren Java-Versionen, bei denen blockierende JDBC-Aufrufe schnell den Thread-Pool erschöpfen konnten.[5]

---

## 6. Datenfluss und UI-Integration (Compose Multiplatform 1.10.0)

Compose 1.10.0-rc02 bringt Verbesserungen im Lifecycle-Management und Rendering.

### 6.1 Repository Pattern mit Flow
Da SQLDelight `Flow` Extensions bietet (`coroutines-extensions`), können wir reaktive Datenströme aufbauen.

```kotlin
// commonMain/kotlin/data/TaskRepository.kt
class TaskRepository(private val db: AppDatabase) {
    // Da generateAsync=true, nutzen wir awaitAsList() für One-Shots
    // und asFlow() für Beobachtung.
    
    val tasks: Flow<List<Task>> = db.taskQueries.selectAll()
      .asFlow() // Erzeugt einen Flow, der bei Datenbankänderungen emittiert
      .mapToList(Dispatchers.IO) // Mapped das Resultat asynchron auf eine Liste
}
```

### 6.2 UI-Integration
In Compose nutzen wir `collectAsState` (oder `collectAsStateWithLifecycle` aus den Lifecycle-Libraries, die nun besser in CMP integriert sind).

```kotlin
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    // Der Initialwert ist wichtig, da der erste DB-Zugriff asynchron ist
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    
    LazyColumn {
        items(tasks) { task ->
            TaskRow(task)
        }
    }
}
```

**Wichtiger Hinweis zu Compose 1.10.0-rc02:**
Es gab Berichte über Probleme beim Laden von Ressourcen in Kombination mit AGP 9.0.0-rc02.[20] Da wir uns auf Desktop und Web konzentrieren, betrifft uns das AGP (Android Gradle Plugin) Problem primär nicht, aber es zeigt, dass RC-Versionen Vorsicht erfordern. Für Desktop/Web ist Compose 1.10.0-rc02 stabil genug und bietet essentielle Fixes für Accessibility im Web.[6]

---

## 7. Herausforderungen und Risiken

### 7.1 "Transient Database" im Web
Das häufigste Problem: Wenn die HTTP-Header (COOP/COEP) fehlen, funktioniert `SharedArrayBuffer` nicht. SQLite Wasm fällt dann stillschweigend auf ein In-Memory-Backend zurück. Der Nutzer merkt nichts, bis er den Tab neu lädt und alle Daten weg sind.
*   **Mitigation:** Implementieren Sie einen Check beim Start (wie im JS-Code in 4.2 gezeigt), der explizit prüft, ob OPFS aktiv ist, und dem Nutzer andernfalls eine Warnung anzeigt ("Daten werden nicht gespeichert!").

### 7.2 Concurrency Limits (SQLITE_BUSY)
SQLite erlaubt nur einen Writer zur gleichen Zeit.
*   **Web:** Wenn der Nutzer zwei Tabs öffnet, und jeder Tab seinen eigenen Worker mit eigener Verbindung zur *gleichen* OPFS-Datei hat, wird der zweite Tab beim Schreiben in einen `SQLITE_BUSY` Fehler laufen, da OPFS exklusive Locks verwendet.
*   **Lösung:** Nutzung eines **SharedWorker**, der die Datenbankverbindung hält. Alle Tabs kommunizieren mit diesem einen SharedWorker. Dies erhöht die Komplexität der Implementierung signifikant, ist aber für eine robuste Produktions-App notwendig.[21, 22] Alternativ: Nutzung von `navigator.locks` API, um sicherzustellen, dass nur ein Tab Schreibrechte hat.

### 7.3 Debugging
Das Debuggen von asynchronem SQL-Code in einem Web Worker ist mühsam. `console.log` im Worker ist sichtbar, aber der Stacktrace ist oft nicht hilfreich.
*   **Empfehlung:** Nutzen Sie die SQLDelight IntelliJ Plugin Features intensiv zur Validierung der `.sq` Dateien zur Compile-Zeit.[9] Was kompiliert, ist meistens syntaktisch korrekt. Logische Fehler sollten durch Unit-Tests im `jvmMain` (mit In-Memory DB) abgefangen werden, da die Logik geteilt ist.

## 8. Zusammenfassung

Die Kombination aus **Kotlin 2.3.0**, **Java 25**, **Compose 1.10.0** und **SQLDelight 2.2.1** ermöglicht eine hochmoderne, echte Offline-Applikation.
Der Schlüssel zum Erfolg liegt in der Akzeptanz der Asynchronität. Indem man das System "Async-First" entwirft (erzwungen durch `generateAsync=true`), erfüllt man die strikten Anforderungen des Web-Browsers (OPFS) und kann gleichzeitig auf dem Desktop durch Java 25 Virtual Threads eine hochperformante, blockierungsfreie Ausführung gewährleisten.

Die Architektur ist zukunftssicher, da sie auf Web-Standards (Wasm, OPFS) und modernen JVM-Features aufbaut, anstatt auf veraltete Workarounds (LocalStorage, Blocking IO threads) zu setzen.

---

### Tabellenverzeichnis

**Tabelle 1: Detaillierte Speicher-Technologie im Vergleich**

| Feature | LocalStorage | IndexedDB | SQL.js (Memory) | **SQLite Wasm + OPFS** |
| :--- | :--- | :--- | :--- | :--- |
| **Persistenz** | Ja | Ja | Nein (Transient) | **Ja (Echt)** |
| **Max. Größe** | ~5-10 MB | GB-Bereich | RAM-abhängig | **GB-Bereich** |
| **Zugriff** | Synchron (Blockierend) | Asynchron (Event-basiert) | Synchron (JS Thread) | **Synchron (in Worker)** |
| **Performance** | Langsam | Mittel | Schnell (kleine Daten) | **Nahe Nativ** |
| **Relational?** | Nein (Key-Value) | Nein (Object Store) | Ja | **Ja** |
| **KMP Eignung** | Gering | Mittel | Mittel | **Hoch (via SQLDelight)** |

**Tabelle 2: SQLDelight Konfigurations-Auswirkungen**

| Einstellung | `generateAsync = false` (Standard) | `generateAsync = true` (Erforderlich) |
| :--- | :--- | :--- |
| **Query Return Type** | `T` (z.B. `List<Task>`) | `suspend () -> T` |
| **Driver Interface** | `SqlDriver` | `SqlDriver` (Async Methoden) |
| **JVM Verhalten** | Blockierend | Blockierend (innerhalb Suspend) |
| **JS/Wasm Verhalten** | Nicht unterstützt (für OPFS) | Unterstützt (Promise-basiert) |
| **Komplexität** | Niedrig | Mittel-Hoch |

Diese Analyse bestätigt, dass Ihr angestrebter Stack nicht nur möglich, sondern die derzeit leistungsfähigste Konfiguration für Cross-Platform-Persistenz ist.
