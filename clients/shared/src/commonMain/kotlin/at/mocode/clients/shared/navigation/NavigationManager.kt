package at.mocode.clients.shared.navigation

import at.mocode.clients.shared.presentation.actions.AppAction
import at.mocode.clients.shared.presentation.store.AppStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Navigation manager for handling routing and navigation logic
 */
class NavigationManager(
    private val store: AppStore
) {

    /**
     * Current route as a flow
     */
    val currentRoute: Flow<String> = store.state.map { it.navigation.currentRoute }

    /**
     * Navigation history as a flow
     */
    val navigationHistory: Flow<List<String>> = store.state.map { it.navigation.history }

    /**
     * Can go back flag as a flow
     */
    val canGoBack: Flow<Boolean> = store.state.map { it.navigation.canGoBack }

    /**
     * Navigate to a specific route
     */
    fun navigateTo(route: String) {
        store.dispatch(AppAction.Navigation.NavigateTo(route))
    }

    /**
     * Navigate back to the previous route
     */
    fun navigateBack() {
        store.dispatch(AppAction.Navigation.NavigateBack)
    }

    /**
     * Replace current route without adding to history
     */
    fun replaceRoute(route: String) {
        store.dispatch(AppAction.Navigation.UpdateHistory(route))
    }

    /**
     * Clear navigation history and navigate to the route
     */
    fun navigateAndClearHistory(route: String) {
        // First clear by replacing with the new route
        store.dispatch(AppAction.Navigation.UpdateHistory(route))
    }

    /**
     * Get current route value (non-reactive)
     */
    fun getCurrentRoute(): String = store.state.value.navigation.currentRoute

    /**
     * Check if we can navigate back
     */
    fun canNavigateBack(): Boolean = store.state.value.navigation.canGoBack
}

/**
 * Route definitions for the application
 */
object Routes {
    const val HOME = "/"
    const val LOGIN = "/login"
    const val DASHBOARD = "/dashboard"
    const val PROFILE = "/profile"
    const val SETTINGS = "/settings"
    const val PING = "/ping"

    // Auth-related routes
    object Auth {
        const val LOGIN = "/auth/login"
        const val LOGOUT = "/auth/logout"
        const val REGISTER = "/auth/register"
        const val FORGOT_PASSWORD = "/auth/forgot-password"
    }

    // Admin routes
    object Admin {
        const val DASHBOARD = "/admin/dashboard"
        const val USERS = "/admin/users"
        const val SETTINGS = "/admin/settings"
    }

    // Feature routes
    object Features {
        const val PING = "/features/ping"
        const val REPORTS = "/features/reports"
        const val NOTIFICATIONS = "/features/notifications"
    }
}

/**
 * Route validation and utilities
 */
object RouteUtils {

    /**
     * Check if a route requires authentication
     */
    fun requiresAuth(route: String): Boolean {
        return when {
            route.startsWith("/auth/") && route != Routes.Auth.LOGIN -> false
            route == Routes.HOME -> false
            route == Routes.LOGIN -> false
            else -> true
        }
    }

    /**
     * Check if a route is for admin only
     */
    fun requiresAdmin(route: String): Boolean {
        return route.startsWith("/admin/")
    }

    /**
     * Get the default route for authenticated users
     */
    fun getDefaultAuthenticatedRoute(): String = Routes.DASHBOARD

    /**
     * Get the default route for unauthenticated users
     */
    fun getDefaultUnauthenticatedRoute(): String = Routes.LOGIN

    /**
     * Validate route format
     */
    fun isValidRoute(route: String): Boolean {
        return route.startsWith("/") && route.isNotBlank()
    }

    /**
     * Parse route parameters (simple implementation)
     */
    fun parseRouteParams(route: String): Map<String, String> {
        val params = mutableMapOf<String, String>()

        // Simple query parameter parsing
        if (route.contains("?")) {
            val parts = route.split("?")
            if (parts.size == 2) {
                val queryParams = parts[1].split("&")
                queryParams.forEach { param ->
                    val keyValue = param.split("=")
                    if (keyValue.size == 2) {
                        params[keyValue[0]] = keyValue[1]
                    }
                }
            }
        }

        return params
    }

    /**
     * Get clean route without parameters
     */
    fun getCleanRoute(route: String): String {
        return if (route.contains("?")) {
            route.split("?")[0]
        } else {
            route
        }
    }
}
