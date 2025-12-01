package at.mocode.clients.authfeature.oauth

import at.mocode.clients.shared.core.AppConstants
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private var currentPkceJvm: PkceState? = null

private fun base64UrlNoPad(bytes: ByteArray): String =
  Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

private fun randomUrlSafe(length: Int): String {
  // Generate bytes and Base64 URL encode (will be > length due to encoding)
  val rnd = SecureRandom()
  val bytes = ByteArray(length)
  rnd.nextBytes(bytes)
  return base64UrlNoPad(bytes)
}

private fun sha256Base64Url(input: String): String {
  val md = MessageDigest.getInstance("SHA-256")
  val digest = md.digest(input.toByteArray(Charsets.UTF_8))
  return base64UrlNoPad(digest)
}

actual object OAuthPkceService {
  actual suspend fun startAuth(): PkceState {
    val codeVerifier = randomUrlSafe(64)
    val codeChallenge = sha256Base64Url(codeVerifier)
    val state = randomUrlSafe(16)
    val pkce = PkceState(state, codeVerifier, codeChallenge)
    currentPkceJvm = pkce
    return pkce
  }

  actual fun current(): PkceState? = currentPkceJvm

  actual fun clear() {
    currentPkceJvm = null
  }

  actual fun buildAuthorizeUrl(state: PkceState, redirectUri: String): String {
    val params = listOf(
      "response_type" to OAuthParams.RESPONSE_TYPE,
      "client_id" to AppConstants.KEYCLOAK_CLIENT_ID,
      "redirect_uri" to redirectUri,
      "scope" to OAuthParams.SCOPE,
      "state" to state.state,
      "code_challenge" to state.codeChallenge,
      "code_challenge_method" to state.method
    ).joinToString("&") { (k, v) -> "$k=" + java.net.URLEncoder.encode(v, Charsets.UTF_8) }
    return AppConstants.authorizeEndpoint() + "?" + params
  }
}
