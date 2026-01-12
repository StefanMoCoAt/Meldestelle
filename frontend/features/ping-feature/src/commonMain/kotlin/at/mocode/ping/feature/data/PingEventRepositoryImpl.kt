package at.mocode.ping.feature.data

import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.sync.SyncableRepository
import at.mocode.ping.api.PingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ARCH-BLUEPRINT: This repository implements the generic SyncableRepository
// for a specific entity, bridging the gap between the sync core and the local database.
class PingEventRepositoryImpl(
  private val db: AppDatabase
) : SyncableRepository<PingEvent> {

  // The `since` parameter for our sync is the ID of the last event, not a timestamp.
  override suspend fun getLatestSince(): String? = withContext(Dispatchers.Default) {
    db.appDatabaseQueries.selectLatestPingEventId().executeAsOneOrNull()
  }

  override suspend fun upsert(items: List<PingEvent>) = withContext(Dispatchers.Default) {
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
