Hier ist der Quellcode des Berichts im Markdown-Format:

# Architektonische Resilienz in verteilten Systemen: Ein umfassender Leitfaden zur Implementierung von Offline-First Kotlin Multiplatform Architekturen mit SQLDelight

## Zusammenfassung

Die Softwareentwicklungslandschaft des Jahres 2026, geprägt durch die Veröffentlichung von Kotlin 2.3.0 und Gradle 9.1.0, bietet Entwicklern beispiellose Möglichkeiten zur Vereinheitlichung komplexer Geschäftslogik über Plattformgrenzen hinweg. Dieser Forschungsbericht analysiert detailliert die architektonischen Muster, Implementierungsstrategien und zugrundeliegenden Mechanismen, die für den Aufbau einer robusten, asynchronen Offline-First-Anwendung erforderlich sind. Der Fokus liegt hierbei auf der Integration von SQLDelight in einer Kotlin Multiplatform (KMP) Umgebung, die sowohl Desktop (JVM) als auch Web (Kotlin/JS) Ziele bedient, eingebettet in eine Mikro-Frontend-Architektur.

Ein zentraler Schwerpunkt dieser Arbeit ist die Überbrückung der Dichotomie zwischen der synchronen Natur klassischer JVM-Datenbanktreiber und der inhärent asynchronen, Event-Loop-basierten Umgebung des modernen Web (insbesondere unter Nutzung von Web Workern und OPFS). Darüber hinaus wird die fortgeschrittene Integration von Persistenzschichten in einem Mikro-Frontend-Ökosystem untersucht, um sicherzustellen, dass eine einzige Quelle der Wahrheit („Single Source of Truth“) über unabhängig bereitgestellte Frontend-Einheiten hinweg konsistent bleibt.

## 1. Einleitung und technologischer Kontext (2026)

### 1.1 Die Evolution von Kotlin Multiplatform

Mit der Veröffentlichung von Kotlin 2.3.0 im Dezember 2025 hat sich das Ökosystem von einer experimentellen Technologie zu einem stabilen Standard für Enterprise-Architekturen entwickelt. Während frühere Versionen oft mit Inkonsistenzen zwischen den Compilern (JVM vs. JS/Native) zu kämpfen hatten, bietet der K2-Compiler in Version 2.3.0 eine vereinheitlichte Frontend-IR (Intermediate Representation), die eine robustere statische Analyse und performantere Kompilierung ermöglicht. Dies ist entscheidend für komplexe Multi-Modul-Projekte, wie sie in Mikro-Frontend-Architekturen üblich sind.

### 1.2 Gradle 9.1.0: Die Build-Infrastruktur

Gradle 9.1.0, veröffentlicht im September 2025, hat die Art und Weise, wie KMP-Projekte konfiguriert werden, grundlegend verändert. Mit der vollständigen Unterstützung des „Configuration Cache“ und der strikten „Project Isolation“ zwingt es Entwickler zu sauberen Modulgrenzen. Für unser Szenario bedeutet dies, dass die Abhängigkeiten zwischen dem `shared`-Modul (Datenbank) und den konsumierenden Mikro-Frontends explizit und ohne Seiteneffekte definiert werden müssen, um die parallele Ausführung und inkrementelle Kompilierung nicht zu gefährden.

### 1.3 Die Problemstellung: Synchron vs. Asynchron

Die zentrale Herausforderung bei der Entwicklung einer Cross-Platform-Datenbanklösung liegt in den unterschiedlichen I/O-Modellen der Zielplattformen:

* **JVM (Desktop):** Historisch geprägt durch blockierende I/O-Operationen (JDBC). Ein Datenbankaufruf blockiert den Thread, bis das Ergebnis vorliegt.
* **Kotlin/JS (Web):** Basiert auf einem Single-Threaded Event Loop. Blockierende Operationen sind hier verboten, da sie das UI einfrieren würden. Moderne Web-Architekturen lagern datenintensive Aufgaben in Web Worker aus, die ausschließlich über asynchrone Nachrichten (Promises/Futures) kommunizieren.

SQLDelight 2.0+ adressiert dieses Problem mit der Konfiguration `generateAsync = true`. Dieser Bericht wird detailliert darlegen, wie diese Einstellung genutzt werden kann, um eine einheitliche, asynchrone Schnittstelle zu schaffen, die auf der JVM effizient in Coroutinen gekapselt wird, während sie im Web nativ mit dem asynchronen Modell korrespondiert.

## 2. Architektonische Fundamente

### 2.1 Das Offline-First Paradigma

In einer Offline-First-Architektur fungiert die lokale Datenbank nicht als bloßer Cache, sondern als primäre Quelle der Wahrheit. Die Benutzeroberfläche (UI) kommuniziert niemals direkt mit dem Netzwerk.

| Konzept | Traditionelle Architektur | Offline-First Architektur |
| --- | --- | --- |
| **Datenquelle** | Remote API (REST/GraphQL) | Lokale Datenbank (SQLite) |
| **Lesepfad** | UI ruft Netzwerk auf -> Wartet -> Zeigt an | UI beobachtet Datenbank (Flow) -> Zeigt an |
| **Schreibpfad** | UI sendet an API -> Wartet auf OK -> Aktualisiert UI | UI schreibt in DB -> DB emittiert neue Daten -> Sync im Hintergrund |
| **Netzwerkstatus** | Voraussetzung für Funktionalität | Optional; beeinflusst nur Synchronisation |

Dieses Prinzip der „Inversion of Control“ entkoppelt die User Experience von Netzwerklatenz und -verfügbarkeit. In SQLDelight wird dies durch Reactive Extensions realisiert, die SQL-Abfragen als `Flow<T>` exponieren, die sich bei Datenänderungen automatisch aktualisieren.

### 2.2 Mikro-Frontends in Kotlin/JS

Die Mikro-Frontend-Architektur überträgt die Prinzipien von Microservices auf das Frontend: Die Anwendung wird in vertikale, in sich geschlossene Slices (Features) zerlegt, die unabhängig entwickelt und deployt werden können. Im Kontext von Kotlin/JS und einer geteilten Datenbank stellt dies eine besondere Herausforderung dar: **Das Singleton-Problem**.

Wenn Modul A (z.B. „Dashboard“) und Modul B (z.B. „Einstellungen“) jeweils ihre eigene Instanz der Datenbank-Engine initialisieren, kommt es zu Konflikten beim Zugriff auf die physische Speicherdatei (z.B. im Origin Private File System, OPFS). Da SQLite in der Regel nur einen Schreibzugriff gleichzeitig erlaubt (Single Writer Principle), muss die Architektur einen **Shared Core Kernel** definieren – ein separat kompiliertes Modul, das die Datenbankinstanz hält und via Webpack Module Federation in die Feature-Module injiziert wird.

## 3. Projekt-Setup und Build-Konfiguration

Das Fundament eines stabilen KMP-Projekts ist eine präzise Gradle-Konfiguration. Wir verwenden einen Version Catalog (`libs.versions.toml`), um Konsistenz über alle Module hinweg zu gewährleisten.

### 3.1 Version Catalog (`gradle/libs.versions.toml`)toml

[versions]
kotlin = "2.3.0"
gradle = "9.1.0"
sqldelight = "2.1.0"
coroutines = "1.10.1" # Hypothetische Version passend zu Kotlin 2.3
ktor = "3.1.0"
koin = "4.0.0"
serialization = "1.8.0"

[libraries]

# SQLDelight

sqldelight-gradle = { module = "app.cash.sqldelight:gradle-plugin", version.ref = "sqldelight" }
sqldelight-driver-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-driver-webworker = { module = "app.cash.sqldelight:web-worker-driver", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }

# Kotlin

kotlin-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlin-coroutines-swing = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-swing", version.ref = "coroutines" } # Für Desktop UI

# Web Specifics

npm-sqljs = { module = "sql.js", version = "1.12.0" }
npm-copy-webpack = { module = "copy-webpack-plugin", version = "12.0.0" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }

```

### 3.2 Modulstruktur für Mikro-Frontends
Eine saubere Trennung ist essenziell. Wir empfehlen folgende Struktur:

*   `:shared-kernel` (KMP): Enthält die Datenbankdefinition (`.sq` Dateien), die generierten Interfaces und die Singleton-Instanziierung der Datenbank.
*   `:shared-logic` (KMP): Enthält domänenspezifische Logik, Repositories und ViewModels, die den Kernel nutzen.
*   `:desktop-app` (JVM): Der Einstiegspunkt für die Desktop-Anwendung.
*   `:web-host` (JS): Die „Shell“-Anwendung, die Mikro-Frontends lädt.
*   `:web-feature-a` (JS): Ein eigenständiges Mikro-Frontend.

### 3.3 Konfiguration des Shared-Kernel Moduls
Das `shared-kernel` Modul ist das Herzstück. Hier wird SQLDelight konfiguriert.

**`shared-kernel/build.gradle.kts`**:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // 1. JVM Target für Desktop
    jvm("desktop") 
    
    // 2. JS Target für Web (IR Compiler ist Standard in 2.0+)
    js(IR) {
        // WICHTIG: Als Library kompilieren für Webpack Federation
        binaries.library() 
        generateTypeScriptDefinitions()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.sqldelight.coroutines)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.sqlite)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.webworker)
                // NPM Abhängigkeiten für SQL.js / Worker
                implementation(npm("sql.js", "1.12.0"))
                implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.1.0"))
                implementation(devNpm("copy-webpack-plugin", "12.0.0"))
            }
        }
    }
}

// 3. SQLDelight Konfiguration
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.offlinefirst.db")
            // KRITISCH: Aktiviert Suspend-Funktionen in generierten Interfaces
            generateAsync.set(true) 
            // Stellt sicher, dass das Schema für beide Plattformen kompatibel bleibt
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

```

Die Einstellung `generateAsync.set(true)` ist der entscheidende Hebel. Ohne diese Einstellung generiert SQLDelight blockierende Funktionen (`executeAsList`). Mit ihr werden `suspend` Funktionen (`awaitAsList`) generiert. Dies ist für das Web-Target zwingend erforderlich, da der Web Worker asynchron antwortet.

## 4. Datenbank-Design und Schema-Management

In einer Offline-First-Architektur muss das Datenbankschema robust genug sein, um Synchronisationsstatus zu verwalten.

**`shared-kernel/src/commonMain/sqldelight/com/example/offlinefirst/db/Task.sq`**:

```sql
-- Tabelle für Aufgaben
CREATE TABLE Task (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    is_completed INTEGER AS Boolean NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    -- Synchronisations-Metadaten
    sync_status TEXT NOT NULL DEFAULT 'SYNCED', -- 'SYNCED', 'DIRTY', 'DELETED'
    last_synced_at INTEGER
);

-- Indizes für Performance bei Abfragen
CREATE INDEX idx_task_sync_status ON Task(sync_status);
CREATE INDEX idx_task_created_at ON Task(created_at);

-- Queries (Nutzung von Named Arguments für Kotlin)
selectAll:
SELECT * FROM Task 
WHERE sync_status!= 'DELETED' 
ORDER BY created_at DESC;

selectDirty:
SELECT * FROM Task 
WHERE sync_status = 'DIRTY';

insertOrReplace:
INSERT OR REPLACE INTO Task(id, title, description, is_completed, created_at, updated_at, sync_status, last_synced_at)
VALUES (?,?,?,?,?,?,?,?);

updateCompletion:
UPDATE Task 
SET is_completed = :isCompleted, 
    updated_at = :updatedAt, 
    sync_status = 'DIRTY' 
WHERE id = :id;

markAsSynced:
UPDATE Task 
SET sync_status = 'SYNCED', 
    last_synced_at = :syncedAt 
WHERE id = :id;

softDelete:
UPDATE Task 
SET sync_status = 'DELETED', 
    updated_at = :deletedAt 
WHERE id = :id;

```

**Analyse:**
Wir verwenden „Soft Deletes“ (Markieren als gelöscht statt physischem Löschen), um sicherzustellen, dass Löschvorgänge auch an den Server synchronisiert werden können. Das Feld `sync_status` steuert die Synchronisationslogik.

## 5. Plattform-Implementierung: Die Treiber-Schicht

Hier liegt die größte Komplexität. Wir müssen eine Brücke zwischen der synchronen JVM-Welt und der asynchronen JS-Welt schlagen.

### 5.1 Abstrakte Fabrik

Im `commonMain` definieren wir eine Schnittstelle zur Erstellung des Treibers.

```kotlin
// shared-kernel/src/commonMain/kotlin/DatabaseFactory.kt
interface DatabaseFactory {
    suspend fun createDriver(): SqlDriver
}

// Hilfsfunktion zur Initialisierung
suspend fun createDatabase(factory: DatabaseFactory): AppDatabase {
    val driver = factory.createDriver()
    // Schema-Erstellung muss ebenfalls asynchron erwartet werden
    AppDatabase.Schema.create(driver).await()
    return AppDatabase(driver)
}

```

### 5.2 Desktop (JVM) Implementierung: Der asynchrone Wrapper

Der `JdbcSqliteDriver` ist blockierend. Auch wenn SQLDelight `suspend` Interfaces generiert, führt der zugrunde liegende Treiber I/O auf dem aufrufenden Thread aus.

**`shared-kernel/src/desktopMain/kotlin/DesktopDatabaseFactory.kt`**:

```kotlin
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

class DesktopDatabaseFactory(private val dbPath: String) : DatabaseFactory {
    override suspend fun createDriver(): SqlDriver {
        // JDBC Connection String
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        
        // Initiale Schema-Erstellung prüfen
        // Da wir generateAsync=true nutzen, gibt Schema.create ein Result zurück,
        // auf das wir warten müssen (.await()).
        // Der JDBC Treiber führt dies jedoch synchron aus.
        if (!File(dbPath).exists()) {
            AppDatabase.Schema.create(driver).await()
        } else {
             // Migrationen prüfen (vereinfacht)
             val currentVersion = 1 // Logic to fetch version
             if (AppDatabase.Schema.version > currentVersion) {
                 AppDatabase.Schema.migrate(driver, currentVersion.toLong(), AppDatabase.Schema.version).await()
             }
        }
        return driver
    }
}

```

**Das Async-Paradoxon auf der JVM:**
Das bloße Vorhandensein von `suspend` im Interface macht den JDBC-Treiber nicht nicht-blockierend. Wenn Sie eine Query in `Dispatchers.Main` aufrufen, wird die UI einfrieren, obwohl es eine `suspend fun` ist.
*Lösung:* Die Repository-Schicht (siehe Abschnitt 6) muss zwingend `withContext(Dispatchers.IO)` verwenden, um die Ausführung auf einen Hintergrund-Thread-Pool zu verlagern.

### 5.3 Web (Kotlin/JS) Implementierung: Web Worker & OPFS

Für eine echte Offline-First-Anwendung im Web reicht `localStorage` oder `IndexedDB` oft nicht aus, insbesondere bei komplexen relationalen Daten. Die modernste Lösung (Stand 2026) ist SQLite über WebAssembly (Wasm) mit dem **Origin Private File System (OPFS)** als Backend.

OPFS bietet einen performanten Dateisystemzugriff, der speziell für Datenbanken optimiert ist, erfordert aber zwingend die Ausführung in einem Web Worker, da die synchronen Zugriffsmethoden (`FileSystemSyncAccessHandle`) im Main Thread blockiert sind.

**Schritt 1: Der Worker-Code**
Wir benötigen einen Web Worker, der den SQLite Wasm Code lädt.

**`shared-kernel/src/jsMain/kotlin/WebDatabaseFactory.kt`**:

```kotlin
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

class WebDatabaseFactory : DatabaseFactory {
    override suspend fun createDriver(): SqlDriver {
        // Wir instanziieren einen Worker, der auf einer dedizierten JS-Datei basiert.
        // Webpack muss diese Datei korrekt verarbeiten und bereitstellen.
        val worker = Worker(
            js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
        )
        
        val driver = WebWorkerDriver(worker)
        
        // Initialisierung
        AppDatabase.Schema.create(driver).await()
        
        return driver
    }
}

```

*Hinweis:* Der Standard `@cashapp/sqldelight-sqljs-worker` nutzt oft noch `sql.js` (In-Memory). Für Persistenz via OPFS müssen Sie oft einen **Custom Worker** implementieren, der `sqlite-wasm` lädt und das VFS für OPFS konfiguriert.
Ein solcher Custom Worker Code (JavaScript) sähe in etwa so aus (vereinfacht):

```javascript
// custom-sqlite.worker.js
import { initBackend } from '@sqlite.org/sqlite-wasm';

addEventListener('message', async ({ data }) => {
    // Initialisierung des OPFS VFS
    const sqlite3 = await initBackend();
    const db = new sqlite3.oo1.OpfsDb('/mydb.sqlite3');
    //... Kommunikation mit SQLDelight Driver Protokoll...
});

```

**Schritt 2: Webpack Konfiguration für Worker**
Damit `import.meta.url` korrekt aufgelöst wird, muss Webpack im verbrauchenden Modul (Web-App) konfiguriert werden.

**`web-host/webpack.config.d/sql-worker.js`**:

```javascript
const CopyWebpackPlugin = require('copy-webpack-plugin');

// Kopiert die WASM Binaries an einen Ort, wo der Browser sie laden kann
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: '../../node_modules/sql.js/dist/sql-wasm.wasm',
                to: 'sql-wasm.wasm'
            }
        ]
    })
);

// Fallbacks für Node-Module, die im Browser fehlen
config.resolve.fallback = {
  ...config.resolve.fallback,
    fs: false,
    path: false,
    crypto: false
};

```

## 6. Das Repository-Pattern: Abstraktion der Asynchronität

Das Repository ist die entscheidende Schicht, um die Unterschiede zwischen JVM (Blocking Wrapper) und JS (Native Async) zu verbergen.

### 6.1 Dispatcher Provider

Wir injizieren Dispatcher, um Testbarkeit und Plattformunterschiede zu managen.

```kotlin
// commonMain
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}

// desktopMain
object DesktopDispatcherProvider : DispatcherProvider {
    override val io = Dispatchers.IO
    override val main = Dispatchers.Main
}

// jsMain
object JsDispatcherProvider : DispatcherProvider {
    // In JS gibt es nur einen Thread. Dispatchers.Default ist hier semantisch passend,
    // technisch aber auf dem gleichen Event Loop.
    override val io = Dispatchers.Default 
    override val main = Dispatchers.Main
}

```

### 6.2 TaskRepository Implementierung

```kotlin
class TaskRepository(
    private val db: AppDatabase,
    private val dispatchers: DispatcherProvider
) {
    private val queries = db.taskQueries

    // READ: Reaktiv (Flow)
    // Beobachtet die DB. Wenn sich Daten ändern, emittiert der Flow neu.
    fun getTasks(): Flow<List<Task>> {
        return queries.selectAll()
          .asFlow()
            // WICHTIG: mapToList führt die Query aus.
            // Auf JVM blockiert das! Daher muss es auf Dispatchers.IO verschoben werden.
          .mapToList(dispatchers.io) 
          .flowOn(dispatchers.io)
    }

    // WRITE: Suspend
    suspend fun addTask(title: String) = withContext(dispatchers.io) {
        val id = uuid4().toString() // Benötigt library: com.benasher44:uuid
        val now = Clock.System.now().toEpochMilliseconds()
        
        queries.transaction {
            queries.insertOrReplace(
                id = id,
                title = title,
                description = null,
                is_completed = false,
                created_at = now,
                updated_at = now,
                sync_status = "DIRTY", // Markiert für Sync
                last_synced_at = null
            )
        }
    }
    
    // SYNC Helper
    suspend fun getDirtyTasks(): List<Task> = withContext(dispatchers.io) {
        queries.selectDirty().awaitAsList()
    }
}

```

**Analyse der `mapToList` Problematik:**
Auf der JVM wird der Listener benachrichtigt, wenn sich die Tabelle ändert. Der Flow sammelt neu. `mapToList` iteriert über den Cursor. Da der JDBC-Treiber blockiert, geschieht dies synchron. Ohne `.flowOn(dispatchers.io)` würde dies auf dem UI-Thread passieren (wenn dort collected wird) und Ruckler verursachen. Auf JS wartet `mapToList` auf das Promise des Workers, was den Main Thread nicht blockiert, aber dennoch Rechenzeit beansprucht. Die Verwendung von `flowOn` ist also Best Practice für beide Plattformen.

## 7. Mikro-Frontend Architektur im Web

In einer Mikro-Frontend-Umgebung (z.B. basierend auf Webpack Module Federation) laden wir verschiedene Teile der Anwendung zur Laufzeit nach. Das Risiko: Jedes Mikro-Frontend könnte versuchen, seine eigene Datenbankverbindung zu öffnen. Bei OPFS/SQLite führt dies zum Absturz, da die Datei gelockt ist.

**Lösung: Shared Kernel als Singleton via Module Federation.**

### 7.1 Export des Shared Kernels

Wir müssen sicherstellen, dass Webpack das `shared-kernel` Modul nur einmal lädt und an alle Mikro-Frontends verteilt.

In `shared-kernel` (JS Target):
Wir erstellen einen expliziten Einstiegspunkt (Entry Point) für JS.

```kotlin
// shared-kernel/src/jsMain/kotlin/Entry.kt
@file:JsExport
package com.example.offlinefirst.kernel

@JsExport
class DatabaseProvider {
    private var _database: AppDatabase? = null

    // Initialisierungsmethode, die NUR von der Shell-App aufgerufen wird
    suspend fun initialize() {
        if (_database!= null) return
        val factory = WebDatabaseFactory()
        _database = createDatabase(factory)
    }

    // Zugriffsmethode für Feature-Module
    fun getDatabase(): AppDatabase {
        return _database?: throw Error("Database not initialized! Call initialize() in App Shell.")
    }
}

// Globales Singleton
@JsExport
val dbProvider = DatabaseProvider()

```

### 7.2 Webpack Federation Konfiguration

In der `build.gradle.kts` der konsumierenden Web-Module (Shell und Features) müssen wir Webpack anweisen, das Kotlin-Laufzeitsystem und unseren Kernel zu teilen.

**`web-host/webpack.config.d/federation.js`**:

```javascript
const ModuleFederationPlugin = require("webpack/lib/container/ModuleFederationPlugin");

config.plugins.push(
    new ModuleFederationPlugin({
        name: "app_shell",
        remotes: {
            feature_dashboard: "feature_dashboard@http://localhost:8081/remoteEntry.js",
            feature_settings: "feature_settings@http://localhost:8082/remoteEntry.js"
        },
        shared: {
            // Kotlin Standardbibliothek teilen, um `instanceof` Checks und globalen State zu erhalten
            "kotlin": { singleton: true, eager: true },
            "kotlinx-coroutines-core": { singleton: true, eager: true },
            // Unser kompiliertes Kernel-Modul
            "shared-kernel": { singleton: true, requiredVersion: "1.0.0" }
        }
    })
);

```

**Erklärung „Singleton & Eager“:**

* `singleton: true`: Stellt sicher, dass Webpack nur eine Version der Bibliothek lädt. Dies ist entscheidend für den Datenbank-State.


* `eager: true`: Zwingt Webpack, diese Bibliotheken sofort beim Start zu laden, statt sie asynchron nachzuladen. Dies verhindert Probleme bei der Initialisierung von Kotlin-Objekten (wie `dbProvider`), die beim App-Start verfügbar sein müssen.

## 8. Synchronisation und Offline-Logik (Store5)

Für eine robuste Synchronisation empfiehlt sich die Implementierung einer „Sync Engine“, die unabhängig vom UI läuft.

### 8.1 Die Sync-Engine

Diese Klasse überwacht die Netzwerkverbindung und synchronisiert Daten im Hintergrund.

```kotlin
class SyncEngine(
    private val repository: TaskRepository,
    private val api: TaskApi,
    private val scope: CoroutineScope
) {
    // Einfache StateFlow basierte Netzwerküberwachung (Plattform-spezifisch zu implementieren)
    val isOnline = NetworkMonitor.status // Flow<Boolean>

    fun start() {
        scope.launch {
            isOnline.collect { online ->
                if (online) {
                    syncPush()
                    syncPull()
                }
            }
        }
    }

    private suspend fun syncPush() {
        val dirtyTasks = repository.getDirtyTasks()
        dirtyTasks.forEach { task ->
            try {
                // Sende an API
                val response = api.pushTask(task)
                // Markiere lokal als Synced
                repository.markAsSynced(task.id, response.syncedAt)
            } catch (e: Exception) {
                // Fehlerbehandlung: Exponential Backoff Logik hier einfügen
                console.error("Sync failed for task ${task.id}: $e")
            }
        }
    }
}

```

### 8.2 Hintergrund-Synchronisation

* **Android/JVM:** Hier würde man `WorkManager` nutzen, um den Sync auch dann auszuführen, wenn die App geschlossen ist.


* **Web:** Hier ist die Situation komplexer. Die Background Sync API ist in Browsern noch nicht flächendeckend stabil für alle Szenarien verfügbar. Die gängige Praxis ist, den Sync im Service Worker oder im Shared Worker auszuführen, solange mindestens ein Tab geöffnet ist.
* *Architektur-Tipp:* Nutzen Sie den gleichen Web Worker, der die Datenbank hält, auch für die Synchronisation, um den Main Thread zu entlasten.



## 9. Teststrategien

Das Testen von asynchronem Datenbankcode erfordert besondere Sorgfalt.

### 9.1 In-Memory Tests

Für Unit Tests verwenden wir In-Memory-Treiber.

* **JVM:** `JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)`
* **JS:** Hier können wir den einfachen `sql.js` Treiber ohne Worker nutzen, da Performance im Unit Test sekundär ist und wir keine Persistenz brauchen.

```kotlin
// shared-kernel/src/commonTest/kotlin/TaskRepositoryTest.kt
class TaskRepositoryTest {
    @Test
    fun testInsertAndRead() = runTest {
        // Setup Driver (Platform specific helper)
        val driver = createTestDriver() 
        val db = AppDatabase(driver)
        AppDatabase.Schema.create(driver).await() // Auch im Test await()!

        val repo = TaskRepository(db, TestDispatcherProvider)

        repo.addTask("Test Task")
        
        val tasks = repo.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks.title)
        assertEquals("DIRTY", tasks.sync_status)
    }
}

```

*Hinweis:* `runTest` aus `kotlinx-coroutines-test` ist essenziell, um Coroutinen in Tests deterministisch auszuführen.

## 10. Fazit und Ausblick

Die Implementierung einer Offline-First-Architektur mit Kotlin Multiplatform, SQLDelight und Mikro-Frontends ist im Jahr 2026 eine leistungsfähige, wenn auch komplexe Lösung.

**Schlüsselerkenntnisse:**

1. **Konfiguration ist der Schlüssel:** Die korrekte Einstellung von `generateAsync = true` in SQLDelight und die Webpack Federation Config (`singleton: true`) sind die häufigsten Fehlerquellen.
2. **Threading-Modelle verstehen:** Der Entwickler muss sich stets bewusst sein, ob Code auf der blockierenden JVM oder im asynchronen JS-Event-Loop läuft. Das `DispatcherProvider`-Muster ist hierfür unerlässlich.
3. **Persistenz im Web:** OPFS ist der Game-Changer, der echte SQLite-Performance in den Browser bringt und `IndexedDB` als primären Speicher für komplexe Daten ablöst.

Durch die Einhaltung der in diesem Bericht dargelegten Muster lässt sich eine Anwendung erstellen, die sich für den Nutzer wie eine native App anfühlt – reaktionsschnell, robust gegen Netzwerkfehler und nahtlos über Plattformen hinweg synchronisiert.

---

**Referenzen im Kontext:**
- Kotlin Versionierung und Release-Zyklen.
- Asynchrones Treiber-Verhalten und Konfiguration.
- Web Worker Setup und NPM Abhängigkeiten.
- Web Persistenztechnologien (OPFS).
- Webpack Module Federation Strategien.
- Hintergrund-Synchronisation.

```

```
