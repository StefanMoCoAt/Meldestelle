### 1\. Welche DI-Lösung? (Dependency Injection)

**Entscheidung:** Wir nutzen **Koin**.

**Begründung (ADR):**

* **Warum nicht Dagger/Hilt?** Hilt ist stark auf Android (Context, Lifecycles) fixiert. Dagger ist extrem komplex im Setup für Multiplatform (Kapt/KSP Setup über alle Targets).
* **Warum Koin?** Es ist ein reines Kotlin-Framework ("Service Locator" Pattern). Es funktioniert identisch auf JVM (Desktop), JS (Web) und Android. Es benötigt keine Annotation-Processing-Magie, was die Build-Zeiten im Monorepo niedrig hält.

**Eintrag im Guide:**

```kotlin
// GUIDELINE: Dependency Injection
// Wir nutzen Koin. Module werden im `di` Package des Features definiert.

// 1. Definition (Feature Module)
val inventoryModule = module {
    // Singletons für Services
    single<InventoryRepository> { InventoryRepositoryImpl(get(), get()) }
    
    // ViewModels (Factory scope)
    viewModel { InventoryViewModel(get()) }
}

// 2. Nutzung des ApiClients (Best Practice)
// Wir injizieren IMMER den "apiClient" (mit Auth-Header), niemals den Default Client.
val networkModule = module {
    single(named("apiClient")) { ... } // Konfiguriert in :core:network
}

val myFeatureModule = module {
    single { 
        // Explizites Holen des authentifizierten Clients
        MyFeatureApi(httpClient = get(named("apiClient"))) 
    }
}
```

-----

### 2\. Welche Offline-DB/ORM?

**Entscheidung:** Wir nutzen **SQLDelight**.

**Begründung (ADR):**

* **Warum nicht Room (KMP)?** Room ist für KMP noch sehr neu (Alpha/Beta Status) und bringt viel Overhead mit sich (SQLite Bundling etc.).
* **Warum SQLDelight?**
  1.  **Schema First:** Du schreibst SQL (`.sq`), und Kotlin-Code wird *generiert*. Das zwingt Entwickler dazu, über ihr Datenmodell nachzudenken, bevor sie Code schreiben.
  2.  **Performance:** Es ist extrem leichtgewichtig und typ-sicher.
  3.  **Migrationen:** SQLDelight hat ein exzellentes System für Schema-Migrationen (`1.sqm`, `2.sqm`), was für Desktop-Apps (die nicht einfach "neu geladen" werden können wie Webseiten) essenziell ist.

**Eintrag im Guide:**

> **DB-Guideline:**
>
>   * Jedes Feature definiert sein Schema in `:frontend:core:local-db/src/commonMain/sqldelight/...`.
>   * Business-Logik darf niemals SQL-Strings enthalten. Nutze die generierten `Queries`-Objekte.
>   * Migrationen sind Pflicht bei Schema-Änderungen\! (Kein `DROP TABLE` in Production).

-----

### 3\. Konfliktstrategie bei Sync?

**Entscheidung:** **Optimistic Locking** (Server Wins).

**Begründung (ADR):**

* In einem System mit Offline-Clients ist "Last Write Wins" gefährlich (Lagerbestand wird überschrieben).
* **Strategie:**
  1.  Jedes Entity hat eine `lastUpdated` (Timestamp) Spalte.
  2.  Der Client sendet beim Update die Version mit, die er *kennt*.
  3.  Wenn Server-Version \> Client-Version → **HTTP 409 Conflict**.
  4.  Client muss Daten neu laden (Refresh) und User fragen/informieren.

**Eintrag im Guide:**

```kotlin
// GUIDELINE: Sync & Conflicts
// Das Frontend führt KEIN komplexes Merging durch. 

suspend fun updateStock(item: Item) {
    try {
        api.update(item.id, item.newStock, currentVersion = item.version)
        // Happy Path: DB Update
    } catch (e: ConflictException) { // HTTP 409
        // 1. Markiere Item in UI als "Out of Sync" (Rot)
        // 2. Trigger automatischen Refresh vom Server
        // 3. Zeige User Toast: "Daten waren veraltet. Bitte prüfen."
        repo.refreshSingleItem(item.id)
    }
}
```

-----

### 4\. Error Budgets / SLIs (Stale Data Indikatoren)

**Entscheidung:** **Visual Freshness Indicators** (Ampel-System).

**Begründung (ADR):**

* Ein User muss wissen, ob der Lagerbestand "live" ist oder "von gestern".
* Wir definieren keine harten Timeouts (App blockieren), sondern weiche UI-Hinweise.

**Eintrag im Guide:**

> **UI-Regel "Data Freshness":**
> Jedes Entity in der lokalen DB hat ein Feld `lastSyncedAt`. Das UI reagiert darauf:
>
>   * **\< 5 min:** ✅ Normalzustand (Kein Indikator).
>   * **\> 5 min:** ⚠️ Kleines gelbes "Wolke"-Icon oder ausgegrauter Text (Warnung).
>   * **\> 1 Stunde:** ❌ Roter Banner "Offline-Daten: Bestand nicht garantiert".
>   * **Aktion:** Schreibende Operationen sind bei "Rot" für kritische Bereiche (z.B. Inventur-Abschluss) gesperrt, für unkritische (z.B. Notiz anlegen) erlaubt (Queue).

-----

### 5\. API-Verträge und Kapselung der Feature-Teams

**Entscheidung:** **Loose Coupling via Navigation Routes & Shared Data Models (Core)**.

**Begründung (ADR):**

* Wir wollen vermeiden, dass Team A (Inventory) direkt Klassen von Team B (Checkout) importiert. Das führt zum "Monolithen-Klumpen".
* Wir nutzen **keine** separaten Gradle-Module pro Feature-API (`:inventory-api`, `:inventory-impl`), da dies den Build-Graph unnötig aufbläht ("Gradle Overhead").

**Strategie:**

1.  **Schnittstelle:** Die einzige "Public API" eines Features ist sein `EntryPoint` (Composable) und seine `Route` (String).
2.  **Datenaustausch:**
  * *Minimal:* Über URL-Parameter (IDs). `navigator.navigate("inventory/details/123")`.
  * *Objekte:* Wenn komplexe Objekte geteilt werden müssen (z.B. `UserProfile`), gehören diese in **`:frontend:core:domain`** (Shared Kernel).

**Eintrag im Guide:**

```kotlin
// GUIDELINE: Feature Isolation
// 1. Features importieren NIEMALS andere Features im `build.gradle.kts`.
// 2. Kommunikation nur über Navigation (Router).
// 3. Gemeinsam genutzte Datenobjekte (z.B. UserID, ShopID) liegen in :core:domain.

// FALSCH:
import com.project.features.billing.Invoice // Abhängigkeit zu anderem Feature!

// RICHTIG:
// Feature A navigiert zu Feature B via Route
navigator.navigateTo("billing/create?orderId=123") 
```

-----

### Zusammenfassung für dein Dokument

Diese 5 Punkte schließen den Kreis:

1.  **Koin** hält den Code sauber.
2.  **SQLDelight** hält die Daten sicher.
3.  **Optimistic Locking** verhindert Datenmüll.
4.  **Freshness UI** managed die Erwartungshaltung des Users.
5.  **Core Domain** verhindert Spaghetti-Code zwischen Features.
