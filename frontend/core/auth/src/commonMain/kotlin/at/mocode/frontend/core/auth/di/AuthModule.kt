package at.mocode.frontend.core.auth.di

import at.mocode.frontend.core.auth.data.AuthApiClient
import at.mocode.frontend.core.auth.data.AuthTokenManager
import at.mocode.frontend.core.auth.presentation.LoginViewModel
import at.mocode.frontend.core.network.TokenProvider
import at.mocode.shared.core.AppConstants
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin-Modul für core-auth: stellt AuthTokenManager bereit und bindet ihn als TokenProvider für apiClient.
 */
val authModule = module {
  // Single in-memory token manager
  single { AuthTokenManager() }

  // AuthApiClient with injected baseHttpClient (NOT apiClient)
  single {
    AuthApiClient(
      httpClient = get(named("baseHttpClient")),
      clientSecret = AppConstants.KEYCLOAK_CLIENT_SECRET
    )
  }

  // LoginViewModel
  factory { LoginViewModel(get(), get(), get(named("apiClient"))) }

  // Brücke zum TokenProvider des Kernnetzwerks, ohne dort eine harte Abhängigkeit hinzuzufügen
  single<TokenProvider> {
    // Wir müssen die AuthTokenManager-Instanz erfassen, um Probleme mit dem 'this'-Kontext in JavaScript zu vermeiden.
    val tokenManager = get<AuthTokenManager>()
    object : TokenProvider {
      override fun getAccessToken(): String? {
        return tokenManager.getToken()
      }
    }
  }
}
