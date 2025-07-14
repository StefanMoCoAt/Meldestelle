import at.mocode.config.AppServiceConfiguration
import at.mocode.config.ThemeService
import at.mocode.di.ServiceRegistry
import at.mocode.di.resolve

fun main() {
    println("Testing ComposeApp ServiceLocator implementation...")

    try {
        // Configure app services
        AppServiceConfiguration.configureAppServices()
        println("✓ Services configured successfully")

        // Test ThemeService resolution
        val themeService: ThemeService = ServiceRegistry.serviceLocator.resolve()
        println("✓ ThemeService resolved successfully")

        // Test ThemeService functionality
        val currentTheme = themeService.getCurrentTheme()
        println("✓ Current theme: $currentTheme")

        // Test theme setting
        themeService.setTheme("dark")
        val newTheme = themeService.getCurrentTheme()
        println("✓ Theme changed to: $newTheme")

        println("✓ All ComposeApp ServiceLocator tests passed!")

    } catch (e: Exception) {
        println("✗ Test failed with error: ${e.message}")
        e.printStackTrace()
    }
}
