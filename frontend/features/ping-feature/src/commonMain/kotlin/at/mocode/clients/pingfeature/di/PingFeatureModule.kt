package at.mocode.clients.pingfeature.di

import at.mocode.clients.pingfeature.PingApiKoinClient
import at.mocode.clients.pingfeature.PingViewModel
import at.mocode.ping.api.PingApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
// import org.koin.core.module.dsl.viewModel // This import seems to be problematic or not available in the current Koin version used

val pingFeatureModule = module {
  // Provide PingApi implementation using the shared authenticated apiClient
  single<PingApi> { PingApiKoinClient(get(named("apiClient"))) }

  // Provide PingViewModel
  // Fallback to factory if viewModel DSL is not available or causing issues
  factory { PingViewModel(get()) }
}
