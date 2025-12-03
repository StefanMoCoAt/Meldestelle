package at.mocode.clients.authfeature.oauth

import at.mocode.shared.core.AppConstants

data class PkceState(
  val state: String,
  val codeVerifier: String,
  val codeChallenge: String,
  val method: String = "S256"
)

object OAuthParams {
  const val RESPONSE_TYPE = "code"
  const val SCOPE = "openid"
}

/**
 * expect/actual service to support PKCE across JS and JVM.
 * For the desktop (JVM) target we currently do not start a browser flow,
 * but we provide hashing to keep API parity.
 */
expect object OAuthPkceService {
  /** Starts a PKCE auth attempt and stores transient state in memory. */
  suspend fun startAuth(): PkceState

  /** Returns currently active state if any (not persisted). */
  fun current(): PkceState?

  /** Clears transient state (after success/failure). */
  fun clear()

  /** Builds the authorize URL for the current state. */
  fun buildAuthorizeUrl(state: PkceState, redirectUri: String = AppConstants.webRedirectUri()): String
}
