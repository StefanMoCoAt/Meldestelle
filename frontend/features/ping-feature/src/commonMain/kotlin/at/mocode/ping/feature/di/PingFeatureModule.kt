package at.mocode.ping.feature.di

import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.ping.api.PingApi
import at.mocode.ping.feature.data.PingApiKoinClient
import at.mocode.ping.feature.data.PingEventRepositoryImpl
import at.mocode.ping.feature.domain.PingSyncService
import at.mocode.ping.feature.domain.PingSyncServiceImpl
import at.mocode.ping.feature.presentation.PingViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Konsolidiertes Koin-Modul für die Ping-Funktion (Clean Architecture).
 */
val pingFeatureModule = module {
  // 1. API Client (Data Layer)
  // Verwendet den gemeinsam genutzten, authentifizierten „apiClient“ aus dem Kernnetzwerk.
  single<PingApi> { PingApiKoinClient(get(named("apiClient"))) }

  // 2. Repository (Data Layer)
  single { PingEventRepositoryImpl(get<AppDatabase>()) }

  // 3. Domain Service (Domain Layer)
  // Wraps SyncManager und Repository, um ViewModel von den Implementierungsdetails von SyncManager zu entkoppeln.
  single<PingSyncService> {
    PingSyncServiceImpl(
      syncManager = get(),
      repository = get<PingEventRepositoryImpl>()
    )
  }

  // 4. ViewModel (Presentation Layer)
  // Injects API und Domain Service
  factory {
    PingViewModel(
      apiClient = get(),
      syncService = get()
    )
  }
}
