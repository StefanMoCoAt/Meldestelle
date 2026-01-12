package at.mocode.frontend.core.sync

import at.mocode.core.sync.Syncable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Minimaler Repository-Contract für Delta-Sync.
 */
interface SyncableRepository<T : Syncable> {
  /**
   * Cursor für Delta-Sync.
   *
   * Konvention: UUIDv7 als String (Backend kann `>` vergleichen) oder ein kompatibler Cursor.
   *
   * @return letzter bekannter Cursor lokal oder `null`, wenn noch keine Daten existieren.
   */
  suspend fun getLatestSince(): String?

  /** Insert oder Update (Upsert) der übergebenen Items. */
  suspend fun upsert(items: List<T>)
}

/**
 * Generischer Sync-Manager.
 *
 * Konvention Backend:
 * - GET `/api/{entity-plural}/sync?since={timestamp}`
 * - Response: `List<T>`
 */
class SyncManager(
  val ktorClient: HttpClient
) {

  suspend inline fun <reified T : Syncable> performSync(
    repository: SyncableRepository<T>,
    endpointPath: String
  ) {
    val since = repository.getLatestSince()

    val remoteItems: List<T> = ktorClient
      .get(endpointPath) {
        // `since` optional
        if (since != null) parameter("since", since)
      }
      .body()

    if (remoteItems.isNotEmpty()) {
      repository.upsert(remoteItems)
    }
  }
}
