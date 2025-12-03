package at.mocode.shared.navigation

import at.mocode.shared.presentation.store.AppStore

/**
 * Deep link handling for the application
 */
class DeepLinkHandler(
  private val navigationManager: NavigationManager,
  private val store: AppStore
) {

  /**
   * Deep link configuration
   */
  data class DeepLinkConfig(
    val scheme: String = "meldestelle",
    val host: String = "app",
    val allowedDomains: Set<String> = setOf("meldestelle.com", "localhost")
  )

  private val config = DeepLinkConfig()

  /**
   * Handle a deep link URL
   */
  fun handleDeepLink(url: String): Boolean {
    return try {
      val parsedLink = parseDeepLink(url)
      if (parsedLink != null) {
        processDeepLink(parsedLink)
        true
      } else {
        false
      }
    } catch (e: Exception) {
      // Log error in real implementation
      false
    }
  }

  /**
   * Parse deep link URL into components
   */
  private fun parseDeepLink(url: String): DeepLink? {
    return when {
      url.startsWith("${config.scheme}://") -> parseCustomSchemeLink(url)
      url.startsWith("https://") || url.startsWith("http://") -> parseWebLink(url)
      else -> null
    }
  }

  /**
   * Parse custom scheme deep links (e.g., meldestelle://app/dashboard)
   */
  private fun parseCustomSchemeLink(url: String): DeepLink? {
    val withoutScheme = url.removePrefix("${config.scheme}://")
    val parts = withoutScheme.split("/")

    if (parts.isEmpty() || parts[0] != config.host) {
      return null
    }

    val path = "/" + parts.drop(1).joinToString("/")
    val route = if (path == "/") Routes.HOME else path

    return DeepLink(
      type = DeepLinkType.CUSTOM_SCHEME,
      route = route,
      params = RouteUtils.parseRouteParams(route),
      originalUrl = url
    )
  }

  /**
   * Parse web deep links (e.g., https://meldestelle.com/dashboard)
   */
  private fun parseWebLink(url: String): DeepLink? {
    // Simple URL parsing - in real implementation use proper URL parser
    val urlParts = url.split("/")
    if (urlParts.size < 3) return null

    val domain = urlParts[2]
    if (!config.allowedDomains.contains(domain)) {
      return null
    }

    val path = "/" + urlParts.drop(3).joinToString("/")
    val route = if (path == "/" || path.isEmpty()) Routes.HOME else path

    return DeepLink(
      type = DeepLinkType.WEB_LINK,
      route = route,
      params = RouteUtils.parseRouteParams(route),
      originalUrl = url
    )
  }

  /**
   * Process a parsed deep link
   */
  private fun processDeepLink(deepLink: DeepLink) {
    val authState = store.state.value.auth
    val cleanRoute = RouteUtils.getCleanRoute(deepLink.route)

    // Check if route requires authentication
    if (RouteUtils.requiresAuth(cleanRoute)) {
      if (!authState.isAuthenticated) {
        // Save the intended route and redirect to log in
        saveIntendedRoute(deepLink.route)
        navigationManager.navigateTo(Routes.Auth.LOGIN)
        return
      }
    }

    // Check if route requires admin privileges
    if (RouteUtils.requiresAdmin(cleanRoute)) {
      val hasAdminRole = authState.user?.roles?.contains("admin") ?: false
      if (!hasAdminRole) {
        // Redirect to unauthorized or home
        navigationManager.navigateTo(Routes.HOME)
        return
      }
    }

    // Navigate to the route
    navigationManager.navigateTo(deepLink.route)
  }

  /**
   * Save the intended route for after authentication
   */
  private fun saveIntendedRoute(route: String) {
    // In real implementation, save to persistent storage
    // For now; we'll store it in a simple variable
    intendedRoute = route
  }

  /**
   * Get and clear the intended route
   */
  fun getAndClearIntendedRoute(): String? {
    val route = intendedRoute
    intendedRoute = null
    return route
  }

  /**
   * Check if there's a pending intended route
   */
  fun hasIntendedRoute(): Boolean = intendedRoute != null

  /**
   * Generate a deep link for a route
   */
  fun generateDeepLink(route: String, useCustomScheme: Boolean = true): String {
    return if (useCustomScheme) {
      "${config.scheme}://${config.host}$route"
    } else {
      "https://${config.allowedDomains.first()}$route"
    }
  }

  /**
   * Validate if a route is valid for deep linking
   */
  fun isValidDeepLinkRoute(route: String): Boolean {
    return RouteUtils.isValidRoute(route) &&
      !route.startsWith("/auth/") && // Auth routes shouldn't be deep linked
      route != Routes.Auth.LOGIN
  }

  companion object {
    private var intendedRoute: String? = null
  }
}

/**
 * Deep link data class
 */
data class DeepLink(
  val type: DeepLinkType,
  val route: String,
  val params: Map<String, String>,
  val originalUrl: String
)

/**
 * Types of deep links
 */
enum class DeepLinkType {
  CUSTOM_SCHEME,  // meldestelle://app/route
  WEB_LINK        // https://meldestelle.com/route
}
