package at.mocode.shared.core

/**
 * Shared application configuration constants for clients.
 * These defaults target local development environments.
 */
object AppConstants {
  // Gateway base URL (reverse proxy / API gateway)
  const val GATEWAY_URL: String = "http://localhost:8081"

  // Keycloak configuration
  const val KEYCLOAK_URL: String = "http://localhost:8180"
  const val KEYCLOAK_REALM: String = "meldestelle"

  // Use public client configured in realm import: `web-app`
  const val KEYCLOAK_CLIENT_ID: String = "web-app"

  // Default redirect URI for web PKCE flow (served by Nginx in web image)
  // We use the root path so Keycloak can redirect back to /?code=...
  fun webRedirectUri(): String = "http://localhost:4000/"

  fun registerUrl(): String =
    "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/protocol/openid-connect/registrations?client_id=$KEYCLOAK_CLIENT_ID&response_type=code&redirect_uri=${
      encode(
        webRedirectUri()
      )
    }"

  fun loginUrl(): String =
    "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/protocol/openid-connect/auth?client_id=$KEYCLOAK_CLIENT_ID&response_type=code&redirect_uri=${
      encode(
        webRedirectUri()
      )
    }"

  fun authorizeEndpoint(): String =
    "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/protocol/openid-connect/auth"

  fun tokenEndpoint(): String =
    "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/protocol/openid-connect/token"

  fun desktopDownloadUrl(): String = "http://localhost:4000/downloads/"

  // Helper to URL-encode values (very small percent-encoding sufficient for URIs here)
  private fun encode(value: String): String =
    value.replace("://", ":%2F%2F").replace("/", "%2F").replace(":", "%3A")
}
