package at.mocode.ping.feature.di

import at.mocode.ping.feature.data.PingEventRepositoryImpl
import at.mocode.ping.feature.presentation.PingViewModel
import at.mocode.frontend.core.localdb.AppDatabase
import org.koin.dsl.module

// Renamed to avoid conflict with clients.pingfeature.di.pingFeatureModule
val pingSyncFeatureModule = module {
  // Provides the ViewModel for the Ping feature.
  factory<PingViewModel> {
    PingViewModel(
      syncManager = get(),
      pingEventRepository = get()
    )
  }

  // Provides the concrete repository implementation for PingEvents.
  single<PingEventRepositoryImpl> { PingEventRepositoryImpl(get<AppDatabase>()) }
}
