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

  // Der `since`-Parameter für unsere Synchronisierung ist die ID des letzten Ereignisses, kein Zeitstempel.
  override suspend fun getLatestSince(): String? {
    println("PingEventRepositoryImpl: getLatestSince called - using corrected async implementation")
    // FIX: Verwenden Sie .awaitAsOneOrNull() für asynchrone Treiber anstelle des blockierenden .executeAsOneOrNull().
    return db.appDatabaseQueries.selectLatestPingEventId().awaitAsOneOrNull()
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
