package at.mocode.frontend.core.auth.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime

/**
 * Client-side permission enumeration that mirrors server-side BerechtigungE
 */
@Serializable
enum class Permission {
  // Person management
  PERSON_READ,
  PERSON_CREATE,
  PERSON_UPDATE,
  PERSON_DELETE,

  // Club management
  VEREIN_READ,
  VEREIN_CREATE,
  VEREIN_UPDATE,
  VEREIN_DELETE,

  // Event management
  VERANSTALTUNG_READ,
  VERANSTALTUNG_CREATE,
  VERANSTALTUNG_UPDATE,
  VERANSTALTUNG_DELETE,

  // Horse management
  PFERD_READ,
  PFERD_CREATE,
  PFERD_UPDATE,
  PFERD_DELETE
}

/**
 * JWT token payload for basic validation and permissions extraction
 */
@Serializable
data class JwtPayload(
  val sub: String? = null,        // User ID
  val username: String? = null,   // Username
  val exp: Long? = null,          // Expiration timestamp
  val iat: Long? = null,          // Issued at timestamp
  val iss: String? = null,        // Issuer
  val permissions: List<String>? = null  // Permissions array
)

/**
 * Authentication state
 */
data class AuthState(
  val isAuthenticated: Boolean = false,
  val token: String? = null,
  val userId: String? = null,
  val username: String? = null,
  val permissions: List<Permission> = emptyList()
)

/**
 * Secure in-memory JWT token manager
 *
 * For web clients, storing tokens in memory is the most secure approach
 * to prevent XSS attacks. The token is lost when the browser tab is closed
 * or refreshed, requiring re-authentication.
 */
class AuthTokenManager {

  private var currentToken: String? = null
  private var tokenPayload: JwtPayload? = null

  private val _authState = MutableStateFlow(AuthState())
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  /**
   * Store JWT token in memory
   */
  fun setToken(token: String) {
    currentToken = token
    tokenPayload = parseJwtPayload(token)

    // Parse permissions from token payload
    val permissions = tokenPayload?.permissions?.mapNotNull { permissionString ->
      try {
        Permission.valueOf(permissionString)
      } catch (e: IllegalArgumentException) {
        // Ignore unknown permissions
        null
      }
    } ?: emptyList()

    _authState.value = AuthState(
      isAuthenticated = true,
      token = token,
      userId = tokenPayload?.sub,
      username = tokenPayload?.username,
      permissions = permissions
    )
  }

  /**
   * Get current JWT token
   */
  fun getToken(): String? = currentToken

  /**
   * Check if we have a valid (non-expired) token
   */
  @OptIn(ExperimentalTime::class)
  fun hasValidToken(): Boolean {
    val token = currentToken ?: return false
    val payload = tokenPayload ?: return false

    // Check expiration
    val expiration = payload.exp ?: return false
    val currentTime = kotlin.time.Clock.System.now().epochSeconds

    return currentTime < expiration
  }

  /**
   * Clear token from memory (logout)
   */
  fun clearToken() {
    currentToken = null
    tokenPayload = null

    _authState.value = AuthState()
  }

  /**
   * Get user ID from token
   */
  fun getUserId(): String? = tokenPayload?.sub

  /**
   * Get username from token
   */
  fun getUsername(): String? = tokenPayload?.username

  /**
   * Get current user permissions
   */
  fun getPermissions(): List<Permission> = _authState.value.permissions

  /**
   * Check if user has a specific permission
   */
  fun hasPermission(permission: Permission): Boolean {
    return _authState.value.permissions.contains(permission)
  }

  /**
   * Check if user has any of the specified permissions
   */
  fun hasAnyPermission(vararg permissions: Permission): Boolean {
    return permissions.any { _authState.value.permissions.contains(it) }
  }

  /**
   * Check if user has all of the specified permissions
   */
  fun hasAllPermissions(vararg permissions: Permission): Boolean {
    return permissions.all { _authState.value.permissions.contains(it) }
  }

  /**
   * Check if user can perform read operations
   */
  fun canRead(): Boolean {
    return hasAnyPermission(
      Permission.PERSON_READ,
      Permission.VEREIN_READ,
      Permission.VERANSTALTUNG_READ,
      Permission.PFERD_READ
    )
  }

  /**
   * Check if user can perform create operations
   */
  fun canCreate(): Boolean {
    return hasAnyPermission(
      Permission.PERSON_CREATE,
      Permission.VEREIN_CREATE,
      Permission.VERANSTALTUNG_CREATE,
      Permission.PFERD_CREATE
    )
  }

  /**
   * Check if user can perform update operations
   */
  fun canUpdate(): Boolean {
    return hasAnyPermission(
      Permission.PERSON_UPDATE,
      Permission.VEREIN_UPDATE,
      Permission.VERANSTALTUNG_UPDATE,
      Permission.PFERD_UPDATE
    )
  }

  /**
   * Check if user can perform delete operations (admin-level)
   */
  fun canDelete(): Boolean {
    return hasAnyPermission(
      Permission.PERSON_DELETE,
      Permission.VEREIN_DELETE,
      Permission.VERANSTALTUNG_DELETE,
      Permission.PFERD_DELETE
    )
  }

  /**
   * Check if user is admin (has delete permissions)
   */
  fun isAdmin(): Boolean = canDelete()

  /**
   * Check if token expires within specified minutes
   */
  @OptIn(ExperimentalTime::class)
  fun isTokenExpiringSoon(minutesThreshold: Int = 5): Boolean {
    val payload = tokenPayload ?: return false
    val expiration = payload.exp ?: return false
    val currentTime = kotlin.time.Clock.System.now().epochSeconds
    val thresholdTime = currentTime + (minutesThreshold * 60)

    return expiration <= thresholdTime
  }

  /**
   * Parse JWT payload for basic validation and user info extraction
   * Note: This is for client-side info extraction only, not security validation
   */
  @OptIn(ExperimentalEncodingApi::class)
  private fun parseJwtPayload(token: String): JwtPayload? {
    return try {
      val parts = token.split(".")
      if (parts.size != 3) return null

      // Decode the payload (second part)
      val payloadJson = Base64.decode(parts[1]).decodeToString()

      // First try to parse with standard approach
      val basicPayload = try {
        Json.decodeFromString<JwtPayload>(payloadJson)
      } catch (e: Exception) {
        // If that fails, extract manually
        null
      }

      // If basic parsing succeeded and has permissions, return it
      if (basicPayload != null && basicPayload.permissions != null) {
        return basicPayload
      }

      // Otherwise, extract permissions manually from JSON string
      val permissions = extractPermissionsFromJson(payloadJson)

      // Return payload with manually extracted permissions
      JwtPayload(
        sub = basicPayload?.sub,
        username = basicPayload?.username,
        exp = basicPayload?.exp,
        iat = basicPayload?.iat,
        iss = basicPayload?.iss,
        permissions = permissions
      )
    } catch (e: Exception) {
      // Failed to parse - token might be invalid format
      null
    }
  }

  /**
   * Extract permissions array from JSON string using simple string parsing
   */
  private fun extractPermissionsFromJson(jsonString: String): List<String>? {
    return try {
      // Simple regex to find permissions array
      val permissionsRegex = """"permissions":\s*\[(.*?)\]""".toRegex()
      val match = permissionsRegex.find(jsonString)

      match?.let {
        val permissionsContent = it.groupValues[1]
        if (permissionsContent.isBlank()) return emptyList()

        // Extract individual permission strings
        val permissions = permissionsContent
          .split(",")
          .mapNotNull { permission ->
            permission.trim()
              .removePrefix("\"")
              .removeSuffix("\"")
              .takeIf { it.isNotBlank() }
          }
        permissions
      }
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Get token with Bearer prefix for HTTP headers
   */
  fun getBearerToken(): String? {
    val token = getToken() ?: return null
    return "Bearer $token"
  }

  /**
   * Refresh token if needed based on expiry
   */
  suspend fun refreshTokenIfNeeded(authApiClient: AuthApiClient): Boolean {
    if (!isTokenExpiringSoon()) return true

    val currentToken = getToken() ?: return false

    val refreshResponse = authApiClient.refreshToken(currentToken)
    if (refreshResponse.success && refreshResponse.token != null) {
      setToken(refreshResponse.token)
      return true
    }

    // Refresh failed, clear token
    clearToken()
    return false
  }
}
