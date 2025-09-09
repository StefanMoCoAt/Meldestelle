package at.mocode.client.kobweb.di

import at.mocode.client.data.service.PingService
import at.mocode.client.kobweb.config.AppConfig
import at.mocode.client.ui.viewmodel.PingViewModel

/**
 * Simple dependency injection container for the Kobweb application.
 * Provides centralized service management and lifecycle handling.
 */
object ServiceProvider {

    // Lazy initialization of services
    private val _pingService by lazy {
        PingService(AppConfig.baseUrl)
    }

    // Track created ViewModels for cleanup
    private val createdViewModels = mutableListOf<PingViewModel>()

    /**
     * Get the singleton PingService instance
     */
    fun getPingService(): PingService = _pingService

    /**
     * Create a new PingViewModel instance.
     * Note: ViewModels should typically be created per screen/component
     * to maintain proper state isolation.
     */
    fun createPingViewModel(): PingViewModel {
        val viewModel = PingViewModel(_pingService)
        createdViewModels.add(viewModel)
        return viewModel
    }

    /**
     * Cleanup a specific ViewModel
     */
    fun cleanupViewModel(viewModel: PingViewModel) {
        viewModel.dispose()
        createdViewModels.remove(viewModel)
    }

    /**
     * Cleanup all resources when the application is shutting down.
     * Should be called when the app is being destroyed.
     */
    fun cleanup() {
        createdViewModels.forEach { it.dispose() }
        createdViewModels.clear()
    }
}
