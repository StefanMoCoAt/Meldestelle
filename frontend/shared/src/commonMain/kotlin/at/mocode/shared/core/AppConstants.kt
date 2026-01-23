package at.mocode.shared.core

/**
 * Shared application configuration constants for clients.
 * These defaults target local development environments.
 */
object AppConstants {
  // Gateway base URL (reverse proxy / API gateway)
  // Used by NetworkConfig via PlatformConfig
  const val GATEWAY_URL: String = "http://localhost:8081"

  // Keycloak configuration
  const val KEYCLOAK_URL: String = "http://localhost:8180"
  const val KEYCLOAK_REALM: String = "meldestelle"

  // Use 'postman-client' for Desktop App Password Flow (Direct Access Grants enabled)
  // 'web-app' is for Browser Flow (PKCE)
  const val KEYCLOAK_CLIENT_ID: String = "postman-client"

  // DEV ONLY: Client Secret for 'postman-client' (Confidential Client)
  // In Production, this should NEVER be in the frontend code.
  // For the Desktop App Pilot, we use this to simulate a secure client.
  const val KEYCLOAK_CLIENT_SECRET: String = "postman-secret-123"

  // Removed unused browser flow URLs (registerUrl, loginUrl, etc.) as we focus on Desktop App.
}
