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
 * Consolidated Koin module for the Ping Feature (Clean Architecture).
 * Replaces the old 'clients.pingfeature' module.
 */
val pingFeatureModule = module {
  // 1. API Client (Data Layer)
  // Uses the shared authenticated 'apiClient' from Core Network
  single<PingApi> { PingApiKoinClient(get(named("apiClient"))) }

  // 2. Repository (Data Layer)
  single { PingEventRepositoryImpl(get<AppDatabase>()) }

  // 3. Domain Service (Domain Layer)
  // Wraps SyncManager and Repository to decouple ViewModel from SyncManager implementation details
  single<PingSyncService> {
    PingSyncServiceImpl(
      syncManager = get(),
      repository = get<PingEventRepositoryImpl>()
    )
  }

  // 4. ViewModel (Presentation Layer)
  // Injects API and Domain Service
  factory {
    PingViewModel(
      apiClient = get(),
      syncService = get()
    )
  }
}
