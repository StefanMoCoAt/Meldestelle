Hier ist eine Zusammenfassung der spezifischen Breaking Changes beim Upgrade von **Exposed 1.0.0-rc-4 auf die stabile
Version 1.0.0** im Markdown-Format:

# Breaking Changes: JetBrains Exposed 1.0.0-rc-4 -> 1.0.0

Dieses Dokument fasst die technischen Änderungen zusammen, die beim Übergang vom letzten Release Candidate (rc-4) zur
stabilen Version 1.0.0 zu beachten sind.

## 1. Überarbeitung der UUID-Unterstützung (Kotlin Multiplatform)

Aufgrund der Einführung der nativen `kotlin.uuid.Uuid` mussten bestehende Klassen, die auf `java.util.UUID` basieren,
verschoben werden, um Namenskollisionen zu vermeiden.

* **Paket-Migration:** Bestehende UUID-Klassen wurden in ein `.java`-Subpaket verschoben:

* `org.jetbrains.exposed.v1.core.dao.id.UUIDTable` -> `...id.java.UUIDTable`


* `org.jetbrains.exposed.v1.core.UUIDColumnType` -> `...core.java.UUIDColumnType`

* `org.jetbrains.exposed.v1.dao.UUIDEntity` -> `...dao.java.UUIDEntity`

* **Methoden-Änderung:** Die Standardmethode `Table.uuid()` akzeptiert nun ausschließlich `kotlin.uuid.Uuid`.

* **Migrationspfad:** Für die Weiterverwendung von `java.util.UUID` muss stattdessen die neue Extension-Funktion
  `Table.javaUUID()` genutzt werden.

## 2. Refactoring des Transaction Managers

Die Typisierung der Transaction Manager wurde spezifiziert, um besser zwischen JDBC und R2DBC zu unterscheiden.

* **Spezifische Rückgabetypen:** `Database.transactionManager` und `R2dbcDatabase.transactionManager` geben nun
  Instanzen von `JdbcTransactionManager` bzw. `R2dbcTransactionManager` zurück (statt des generischen
  `TransactionManager`).

* **Entfernung von APIs:** Die Methode `TransactionManagerApi.currentOrNull()` wurde entfernt.

* **Ersatz:** Nutzen Sie stattdessen die Extension-Funktionen `JdbcTransactionManager.currentOrNull()` /
  `R2dbcTransactionManager.currentOrNull()` oder die statische Methode `TransactionManager.currentOrNull()`.

## 3. R2DBC API Bereinigungen

Um die API näher an die zugrunde liegenden Treiber-Spezifikationen (io.r2dbc.spi) zu bringen, wurden ungenutzte Methoden
entfernt.

* **R2dbcPreparedStatementApi:** Die Methoden `closeIfPossible()` und `cancel()` wurden entfernt, da sie in der
  R2DBC-Spi keine Entsprechung finden.

* **Methoden-Umbenennung:** `R2dbcTransaction.closeExecutedStatements()` wurde in `.clearExecutedStatements()`
  umbenannt. Diese Methode ist nun nicht mehr suspendierbar (`non-suspending`).

## 4. SQLite & JSONB Automatisierung

Das Handling von JSONB-Spalten in SQLite wurde vereinheitlicht.

* **Automatisches Wrapping:** Bei der Verwendung von `jsonb()` werden Spalten in der `SELECT`-Klausel nun automatisch in
  die SQL-Funktion `JSON()` eingepackt, um die Lesbarkeit zu verbessern.

* **Konfiguration:** Dieses Verhalten kann über den Parameter `castToJsonFormat=false` deaktiviert werden.

* **Core-Interface:** Das Interface `JsonColumnMarker` in `exposed-core` wurde um die Eigenschaft
  `needsBinaryFormatCast` erweitert.

## 5. Sonstige Anpassungen

* **Logging-Level:** Die Protokollierung für Transaction-Retry-Verzögerungen und Rollback-Fehler wurde von `WARN` auf
  `DEBUG` herabgestuft.

* **Transaktions-ID:** Das Feld `Transaction.id` wurde endgültig in `Transaction.transactionId` umbenannt, um
  Shadowing-Probleme mit Benutzer-Code zu vermeiden.
