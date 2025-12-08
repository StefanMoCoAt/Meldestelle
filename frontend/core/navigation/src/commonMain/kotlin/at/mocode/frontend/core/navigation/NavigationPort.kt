package at.mocode.frontend.core.navigation

/**
 * Minimal navigation abstraction used by core navigation components.
 * The actual implementation lives in shells/apps and delegates to the app's router.
 */
interface NavigationPort {
  fun navigateTo(route: String)
}
