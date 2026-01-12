package at.mocode.ping.feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.frontend.core.sync.SyncManager
import at.mocode.ping.api.PingEvent
import at.mocode.ping.feature.data.PingEventRepositoryImpl
import kotlinx.coroutines.launch

class PingViewModel(
  private val syncManager: SyncManager,
  private val pingEventRepository: PingEventRepositoryImpl
) : ViewModel() {

  init {
    // Trigger an initial sync when the ViewModel is created.
    triggerSync()
  }

  fun triggerSync() {
    viewModelScope.launch {
      try {
        syncManager.performSync<PingEvent>(pingEventRepository, "/api/pings/sync")
      } catch (_: Exception) {
        // TODO: Handle sync errors and expose them to the UI
      }
    }
  }
}
