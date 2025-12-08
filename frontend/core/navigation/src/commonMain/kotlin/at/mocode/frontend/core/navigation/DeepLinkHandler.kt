package at.mocode.frontend.core.navigation

import at.mocode.frontend.core.domain.models.AppRoles

/**
 * Deep link handling with minimal auth-aware guard via CurrentUserProvider.
 * This version is self-contained in core:navigation and has no dependency on shared app store.
 */
class DeepLinkHandler(
  private val navigation: NavigationPort,
  private val currentUserProvider: CurrentUserProvider,
) {

  data class DeepLinkConfig(
    val scheme: String = "meldestelle",
    val host: String = "app",
    val allowedDomains: Set<String> = setOf("meldestelle.com", "localhost"),
    val loginRoute: String = Routes.Auth.LOGIN,
  )

  private val config = DeepLinkConfig()

  fun handleDeepLink(url: String): Boolean {
    val parsed = parseDeepLink(url) ?: return false
    return processDeepLink(parsed)
  }

  private fun processDeepLink(deepLink: DeepLink): Boolean {
    val route = cleanRoute(deepLink.route)

    // If route requires auth and user is missing → redirect to login
    if (requiresAuth(route)) {
      val user = currentUserProvider.getCurrentUser()
      if (user == null) {
        navigation.navigateTo(config.loginRoute)
        return true
      }
      // Admin section guard: requires ADMIN role
      if (requiresAdmin(route)) {
        val isAdmin = user.roles.contains(AppRoles.ADMIN)
        if (!isAdmin) {
          navigation.navigateTo(Routes.HOME)
          return true
        }
      }
    }

    navigation.navigateTo(deepLink.route)
    return true
  }

  private fun parseDeepLink(url: String): DeepLink? {
    return when {
      url.startsWith("${config.scheme}://") -> parseCustomScheme(url)
      url.startsWith("https://") || url.startsWith("http://") -> parseWeb(url)
      else -> null
    }
  }

  private fun parseCustomScheme(url: String): DeepLink? {
    val withoutScheme = url.removePrefix("${config.scheme}://")
    val parts = withoutScheme.split("/")
    if (parts.isEmpty() || parts[0] != config.host) return null
    val path = "/" + parts.drop(1).joinToString("/")
    val route = if (path.isBlank()) Routes.HOME else path
    return DeepLink(DeepLinkType.CUSTOM_SCHEME, route, url)
  }

  private fun parseWeb(url: String): DeepLink? {
    val urlParts = url.split("/")
    if (urlParts.size < 3) return null
    val domain = urlParts[2]
    if (!config.allowedDomains.contains(domain)) return null
    val path = "/" + urlParts.drop(3).joinToString("/")
    val route = if (path.isBlank() || path == "/") Routes.HOME else path
    return DeepLink(DeepLinkType.WEB_LINK, route, url)
  }

  private fun cleanRoute(route: String): String = route.substringBefore("?")

  private fun requiresAuth(route: String): Boolean {
    if (route == Routes.HOME) return false
    if (route == Routes.LOGIN || route == Routes.Auth.LOGIN) return false
    if (route.startsWith("/auth/") && route != Routes.Auth.LOGIN) return false
    return true
  }

  private fun requiresAdmin(route: String): Boolean = route.startsWith("${Routes.Admin.ROOT}/")

  fun generateDeepLink(route: String, useCustomScheme: Boolean = true): String =
    if (useCustomScheme) "${config.scheme}://${config.host}$route" else "https://${config.allowedDomains.first()}$route"
}
