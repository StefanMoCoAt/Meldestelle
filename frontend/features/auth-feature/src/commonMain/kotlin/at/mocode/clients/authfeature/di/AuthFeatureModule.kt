package at.mocode.clients.authfeature.di

import at.mocode.clients.authfeature.AuthApiClient
import at.mocode.clients.authfeature.AuthTokenManager
import at.mocode.clients.authfeature.LoginViewModel
import at.mocode.frontend.core.network.TokenProvider
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for auth-feature: provides AuthTokenManager and binds it as TokenProvider for apiClient.
 */
val authFeatureModule = module {
  // Single in-memory token manager
  single { AuthTokenManager() }

  // AuthApiClient with injected apiClient
  single { AuthApiClient(get(named("apiClient"))) }

  // LoginViewModel
  factory { LoginViewModel(get(), get(), get(named("apiClient"))) }

  // Bridge to core network TokenProvider without adding a hard dependency there
  single<TokenProvider> {
    object : TokenProvider {
      override fun getAccessToken(): String? {
        val token = get<AuthTokenManager>().getToken()
        return token
      }
    }
  }
}
