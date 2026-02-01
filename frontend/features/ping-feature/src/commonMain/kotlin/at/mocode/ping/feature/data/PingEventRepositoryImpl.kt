package at.mocode.ping.feature.data

import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.sync.SyncableRepository
import at.mocode.ping.api.PingEvent
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

/**
 ** ARCH-BLUEPRINT: Dieses Repository implementiert das generische Syncable Repository
 ** für eine bestimmte Entität und überbrückt so die Lücke zwischen dem Sync-Core und der
 ** lokalen Datenbank.
 */
class PingEventRepositoryImpl(
  private val db: AppDatabase
) : SyncableRepository<PingEvent> {

  // Der `since`-Parameter für unsere Synchronisierung ist der Zeitstempel des letzten Ereignisses.
  // Das Backend erwartet einen Long (Timestamp), keinen String (UUID).
  override suspend fun getLatestSince(): String? {
    println("PingEventRepositoryImpl: getLatestSince called - fetching latest timestamp")
    // Wir holen den letzten Timestamp aus der DB.
    val lastModified = db.appDatabaseQueries.selectLatestPingEventTimestamp().awaitAsOneOrNull()

    // Wir geben ihn als String zurück, da das Interface String? erwartet.
    // Der SyncManager wird ihn als Parameter "since" an den Request hängen.
    // Das Backend erwartet "since" als Long, aber HTTP Parameter sind Strings.
    // Spring Boot konvertiert "123456789" automatisch in Long 123456789.
    return lastModified?.toString()
  }

  override suspend fun upsert(items: List<PingEvent>) {
    // Führen Sie Massenoperationen immer innerhalb einer Transaktion durch.
    db.transaction {
      items.forEach { event ->
        db.appDatabaseQueries.upsertPingEvent(
          id = event.id,
          message = event.message,
          last_modified = event.lastModified
        )
      }
    }
  }
}
