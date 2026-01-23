package at.mocode.frontend.core.auth.di

import at.mocode.frontend.core.auth.data.AuthApiClient
import at.mocode.frontend.core.auth.data.AuthTokenManager
import at.mocode.frontend.core.auth.presentation.LoginViewModel
import at.mocode.frontend.core.network.TokenProvider
import at.mocode.shared.core.AppConstants
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for core-auth: provides AuthTokenManager and binds it as TokenProvider for apiClient.
 */
val authModule = module {
  // Single in-memory token manager
  single { AuthTokenManager() }

  // AuthApiClient with injected apiClient and DEV client secret
  single {
    AuthApiClient(
      httpClient = get(named("apiClient")),
      clientSecret = AppConstants.KEYCLOAK_CLIENT_SECRET
    )
  }

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
