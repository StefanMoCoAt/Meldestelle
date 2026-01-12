package at.mocode.frontend.core.sync.di

import at.mocode.frontend.core.sync.SyncManager
import org.koin.dsl.module

/**
 * Zentrales Koin-Modul für den Sync-Core.
 */
val syncModule = module {
  // Provides a singleton instance of SyncManager, using the globally provided HttpClient.
  single { SyncManager(get()) }
}
