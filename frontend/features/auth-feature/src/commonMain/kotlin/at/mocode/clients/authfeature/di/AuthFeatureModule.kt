package at.mocode.clients.authfeature.di

import at.mocode.clients.authfeature.AuthTokenManager
import at.mocode.frontend.core.network.TokenProvider
import org.koin.dsl.module

/**
 * Koin module for auth-feature: provides AuthTokenManager and binds it as TokenProvider for apiClient.
 */
val authFeatureModule = module {
  // Single in-memory token manager
  single { AuthTokenManager() }

  // Bridge to core network TokenProvider without adding a hard dependency there
  single<TokenProvider> {
    object : TokenProvider {
      override fun getAccessToken(): String? = get<AuthTokenManager>().getToken()
    }
  }
}
