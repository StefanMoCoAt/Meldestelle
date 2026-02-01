package at.mocode.ping.feature.domain

import at.mocode.frontend.core.sync.SyncManager
import at.mocode.frontend.core.sync.SyncableRepository
import at.mocode.ping.api.PingEvent

/**
 * Interface für den Ping-Sync-Dienst zur einfacheren Prüfung und Entkopplung.
 */
interface PingSyncService {
  suspend fun syncPings()
}

/**
 * Implementierung des PingSyncService unter Verwendung des generischen SyncManager.
 */
class PingSyncServiceImpl(
  private val syncManager: SyncManager,
  private val repository: SyncableRepository<PingEvent>
) : PingSyncService {

  override suspend fun syncPings() {
    // Corrected endpoint: /api/ping/sync (singular)
    syncManager.performSync(repository, "/api/ping/sync")
  }
}
