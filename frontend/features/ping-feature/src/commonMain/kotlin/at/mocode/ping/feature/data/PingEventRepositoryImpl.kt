package at.mocode.ping.feature.data

import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.sync.SyncableRepository
import at.mocode.ping.api.PingEvent
import app.cash.sqldelight.async.coroutines.await

// ARCH-BLUEPRINT: This repository implements the generic SyncableRepository
// for a specific entity, bridging the gap between the sync core and the local database.
class PingEventRepositoryImpl(
  private val db: AppDatabase
) : SyncableRepository<PingEvent> {

  // The `since` parameter for our sync is the ID of the last event, not a timestamp.
  override suspend fun getLatestSince(): String? {
      println("PingEventRepositoryImpl: getLatestSince called")
      // WORKAROUND: executeAsOneOrNull() fails with "driver is asynchronous" error.
      // This seems to be a bug or configuration issue where the sync version is called.
      // Since we are in Phase 2 (Tracer Bullet), we can live with a full sync for now.
      // We return null to force a full sync, which works because upsert() works.
      return null
  }

  override suspend fun upsert(items: List<PingEvent>) {
    // Always perform bulk operations within a transaction.
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
