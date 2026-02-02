---
type: Guide
status: DRAFT
owner: Backend Developer
date: 2026-02-02
---

# Database Best Practices & Exposed 1.0.0

Dieser Guide beschreibt den korrekten Umgang mit der Datenbank-Schicht in unseren Backend-Services, basierend auf JetBrains Exposed 1.0.0.

## 1. Architektur-Prinzipien

*   **Trennung:** Datenbank-Zugriffe gehören ausschließlich in die `infrastructure/persistence` Schicht. Services nutzen Repositories (Interfaces), keine direkten Exposed-Aufrufe.
*   **Transaktionen:** Jede geschäftliche Operation sollte in einer Transaktion laufen. Nutze dafür die Helper aus `DatabaseUtils.kt`.

## 2. Nutzung von `DatabaseUtils`

Wir haben zentrale Wrapper für Transaktionen, um Fehlerbehandlung und Logging zu vereinheitlichen.

### 2.1 Transaktionen starten

Nutze immer `transactionResult` (oder die Aliase `readTransaction` / `writeTransaction`), um Exposed-Code auszuführen.

```kotlin
fun findUser(id: UUID): Result<User> = readTransaction {
    // 'this' ist hier eine JdbcTransaction
    UserTable.select { UserTable.id eq id }
        .map { ... }
        .singleOrNull()
}
```

**Wichtig:** Der Lambda-Receiver ist `JdbcTransaction`. Das ermöglicht Zugriff auf Low-Level JDBC Funktionen, falls nötig.

### 2.2 Low-Level SQL (`exec`, `executeUpdate`)

Vermeide rohes SQL, wo immer möglich. Wenn es sein muss (z.B. für Performance-Optimierungen oder spezielle Postgres-Features), beachte folgende Regeln für Exposed 1.0.0:

*   **`exec`:** Nutze immer `explicitStatementType`.
    ```kotlin
    this.exec("SELECT 1", explicitStatementType = StatementType.SELECT) { rs -> ... }
    ```
*   **`executeUpdate`:** Nutze die Helper-Methode `DatabaseUtils.executeUpdate`, da sie sich um das korrekte Schließen von Statements kümmert (Exposed `PreparedStatementApi` ist nicht `AutoCloseable`).

## 3. Exposed 1.0.0 Besonderheiten

*   **UUIDs:** Nutze `Table.javaUUID()` für `java.util.UUID` Spalten. `Table.uuid()` ist für `kotlin.uuid.Uuid` reserviert.
*   **JSONB:** Bei SQLite wird JSON automatisch gewrappt. Prüfe `castToJsonFormat` Flag.

## 4. Fehlerbehandlung

`DatabaseUtils` fängt `SQLException` ab und mappt sie auf unsere Domain-Fehler (`ErrorDto`):
*   Duplicate Key -> `ErrorCodes.DUPLICATE_ENTRY`
*   Foreign Key -> `ErrorCodes.FOREIGN_KEY_VIOLATION`
*   Timeout -> `ErrorCodes.DATABASE_TIMEOUT`

Wirf keine rohen Exceptions aus Repositories.
