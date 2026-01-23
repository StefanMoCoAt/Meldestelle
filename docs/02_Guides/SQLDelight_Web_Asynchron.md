# Architekturstrategien für Asynchrone Persistenz in Kotlin Multiplatform: Eine umfassende Analyse zur Integration von SQLDelight in Web-Umgebungen

## 1. Einleitung und Problemstellung

Die Entwicklung plattformübergreifender Anwendungen mittels Kotlin Multiplatform (KMP) hat in den letzten Jahren einen paradigmatischen Wandel vollzogen. Ein zentraler Bestandteil dieser Architektur ist die Datenpersistenz, für die sich SQLDelight als Industriestandard etabliert hat.

Die Integration der Web-Plattform stellt jedoch eine signifikante architektonische Herausforderung dar. Wie in der Problemstellung korrekt identifiziert, existiert eine fundamentale Diskrepanz zwischen den synchronen I/O-Operationen nativer Plattformen (Android, iOS) und der zwingend asynchronen Natur des Webs. Während native SQLite-Treiber (`AndroidSqliteDriver`, `NativeSqliteDriver`) Datenbankoperationen blockierend ausführen können, erfordert der Browser die Nutzung eines `WebWorkerDriver` und asynchrone Initialisierungsmuster.

Dieser Bericht liefert eine Lösungsarchitektur basierend auf dem "Lazy Async Wrapper"-Muster und Koin.

---

## 2. Theoretisches Fundament: Die Asynchronitäts-Lücke

### 2.1 Native vs. Web-Laufzeitumgebungen

Auf nativen Systemen kann der `SqlDriver` synchron instanziiert werden. Im Browser hingegen nutzt SQLDelight `sql.js` oder `sqlite-wasm` in einem Web Worker. Die Kommunikation erfolgt über Message Passing, was `suspend`-Funktionen für die Initialisierung erzwingt.

### 2.2 Der Paradigmenwechsel mit SQLDelight 2.0

Mit Version 2.0 wurde die Konfiguration `generateAsync` eingeführt:kotlin sqldelight { databases { create("AppDatabase") { packageName.set("com.example.db") generateAsync.set(true) } } }

Setzt man dieses Flag auf `true`, werden alle Datenbankoperationen als `suspend`-Funktionen generiert.[1, 4] Dies ist der erste Schritt zur Vereinheitlichung: Auch native Plattformen nutzen nun (formal) asynchrone Schnittstellen, was den gemeinsamen Code homogenisiert.

---

## 3. Die Lösungsarchitektur: Das "Lazy Async Wrapper"-Muster

Anstatt die Datenbank direkt beim App-Start zu initialisieren (was im Web blockieren oder fehlschlagen würde, wenn der Worker noch nicht bereit ist), kapseln wir den Treiber in einer Wrapper-Klasse.[5, 2]

### 3.1 Definition der Factory

**Datei:** `shared/src/commonMain/kotlin/.../DatabaseDriverFactory.kt`

```kotlin
import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    suspend fun createDriver(): SqlDriver
}

```

### 3.2 Der Database Wrapper

Diese Komponente löst das Problem des Nutzers, indem sie die Initialisierung bis zum ersten Zugriff verzögert und mittels `Mutex` absichert.

**Datei:** `shared/src/commonMain/kotlin/.../DatabaseWrapper.kt`

```kotlin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DatabaseWrapper(private val driverFactory: DatabaseDriverFactory) {
    private var _database: AppDatabase? = null
    private val mutex = Mutex()

    suspend fun get(): AppDatabase {
        _database?.let { return it }
        return mutex.withLock {
            _database?: AppDatabase(driverFactory.createDriver()).also { _database = it }
        }
    }

    // Helper für Repositories
    suspend operator fun <R> invoke(block: suspend (AppDatabase) -> R): R {
        return block(get())
    }
}

```

---

## 4. Implementierung der Plattform-Treiber

### 4.1 Web (Kotlin/Wasm & JS)

Hier liegt der Kern der Lösung: Wir warten explizit auf die Schema-Erstellung (`awaitCreate`), bevor wir den Treiber zurückgeben.

**Datei:** `shared/src/jsMain/kotlin/.../WebDatabaseDriverFactory.kt`

```kotlin
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

class WebDatabaseDriverFactory : DatabaseDriverFactory {
    override suspend fun createDriver(): SqlDriver {
        val worker = Worker(
            js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
        )
        val driver = WebWorkerDriver(worker)
        
        // WICHTIG: Hier wird asynchron gewartet!
        AppDatabase.Schema.create(driver).await()
        return driver
    }
}

```

**Webpack Konfiguration:**
Damit dies funktioniert, muss die `sql-wasm.wasm` Datei korrekt kopiert werden.

```javascript
// webpack.config.d/sqljs.js
const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/sql.js/dist/sql-wasm.wasm'
        ]
    })
);

```

### 4.2 Android (Synchron)

Für Android geben wir den synchronen Treiber einfach in der `suspend`-Funktion zurück.

```kotlin
class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override suspend fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")
    }
}

```

---

## 5. Integration mit Koin

Da der `DatabaseWrapper` selbst leichtgewichtig ist (er erstellt die DB noch nicht im Konstruktor), kann er problemlos als `single` in Koin registriert werden.

```kotlin
val appModule = module {
    single { DatabaseWrapper(get()) }
    single { MyRepository(get()) }
}

```

Das Repository nutzt dann den Wrapper:

```kotlin
class MyRepository(private val dbWrapper: DatabaseWrapper) {
    suspend fun getItems() = dbWrapper { db ->
        db.itemQueries.selectAll().executeAsList()
    }
}

```

## 6. Zusammenfassung

Diese Architektur löst den Konflikt zwischen synchronen und asynchronen Welten durch:

1. **`generateAsync = true`**: Erzwingt `suspend` überall.


2. **Wrapper Pattern**: Kapselt die asynchrone Initialisierung (`await()`) im Web.


3. **Koin Singleton**: Der Wrapper kann sofort injiziert werden, die DB wird erst beim ersten `invoke` geladen.

