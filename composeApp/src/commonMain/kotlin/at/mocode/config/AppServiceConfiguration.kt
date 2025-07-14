package at.mocode.config

import at.mocode.di.ServiceRegistry
import at.mocode.di.register

/**
 * Service configuration for the Compose application.
 * Demonstrates how to use the ServiceLocator pattern in the frontend.
 */
object AppServiceConfiguration {

    /**
     * Initialize services for the compose application
     */
    fun configureAppServices() {
        val serviceLocator = ServiceRegistry.serviceLocator

        // Register frontend-specific services
        registerUIServices(serviceLocator)

        // Register API clients or other services as needed
        // registerApiServices(serviceLocator)
    }

    /**
     * Register UI-related services
     */
    private fun registerUIServices(serviceLocator: at.mocode.di.ServiceLocator) {
        // Example: Register a theme service
        serviceLocator.register<ThemeService> { DefaultThemeService() }

        // Example: Register a navigation service
        serviceLocator.register<NavigationService> { DefaultNavigationService() }

        // Add more UI services as needed
    }

    /**
     * Clear all registered services (useful for testing)
     */
    fun clearAppServices() {
        ServiceRegistry.serviceLocator.clear()
    }
}

/**
 * Example theme service interface
 */
interface ThemeService {
    fun getCurrentTheme(): String
    fun setTheme(theme: String)
}

/**
 * Default implementation of ThemeService
 */
class DefaultThemeService : ThemeService {
    private var currentTheme = "light"

    override fun getCurrentTheme(): String = currentTheme

    override fun setTheme(theme: String) {
        currentTheme = theme
    }
}

/**
 * Example navigation service interface
 */
interface NavigationService {
    fun navigateTo(route: String)
    fun goBack()
}

/**
 * Default implementation of NavigationService
 */
class DefaultNavigationService : NavigationService {
    override fun navigateTo(route: String) {
        // Implementation for navigation
        println("Navigating to: $route")
    }

    override fun goBack() {
        // Implementation for going back
        println("Going back")
    }
}
