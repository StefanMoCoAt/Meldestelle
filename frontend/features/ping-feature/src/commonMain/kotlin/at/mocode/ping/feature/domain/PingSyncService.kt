package at.mocode.ping.feature.domain

import at.mocode.frontend.core.sync.SyncManager
import at.mocode.frontend.core.sync.SyncableRepository
import at.mocode.ping.api.PingEvent

/**
 * Interface for the Ping Sync Service to allow easier testing and decoupling.
 */
interface PingSyncService {
  suspend fun syncPings()
}

/**
 * Implementation of PingSyncService using the generic SyncManager.
 */
class PingSyncServiceImpl(
  private val syncManager: SyncManager,
  private val repository: SyncableRepository<PingEvent>
) : PingSyncService {

  override suspend fun syncPings() {
    syncManager.performSync(repository, "/api/pings/sync")
  }
}
