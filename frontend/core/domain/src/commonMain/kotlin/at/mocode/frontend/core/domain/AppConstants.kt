package at.mocode.frontend.core.domain

object AppConstants {
  // Keycloak Configuration
  // Note: These defaults are for local development.
  // In production, these should be provided via build config or environment variables.
  const val KEYCLOAK_URL = "http://localhost:8180"
  const val KEYCLOAK_REALM = "meldestelle"
  const val KEYCLOAK_CLIENT_ID = "web-app"
  const val KEYCLOAK_CLIENT_SECRET = "" // Public client usually has no secret
}
