package at.mocode.config

import at.mocode.di.ServiceRegistry
import at.mocode.di.register
import at.mocode.di.resolve
import at.mocode.repositories.*
import at.mocode.services.*

/**
 * Configuration class for setting up dependency injection using ServiceLocator.
 * Registers all repositories and services with the ServiceRegistry.
 */
object ServiceConfiguration {

    /**
     * Initialize and configure all services and repositories
     */
    fun configureServices() {
        val serviceLocator = ServiceRegistry.serviceLocator

        // Register repositories
        registerRepositories(serviceLocator)

        // Register services
        registerServices(serviceLocator)
    }

    /**
     * Register all repository implementations
     */
    private fun registerRepositories(serviceLocator: at.mocode.di.ServiceLocator) {
        // Register repository implementations
        serviceLocator.register<PersonRepository> { PostgresPersonRepository() }
        serviceLocator.register<PlatzRepository> { PostgresPlatzRepository() }
        serviceLocator.register<VereinRepository> { PostgresVereinRepository() }
        serviceLocator.register<ArtikelRepository> { PostgresArtikelRepository() }
        serviceLocator.register<AbteilungRepository> { PostgresAbteilungRepository() }
        serviceLocator.register<BewerbRepository> { PostgresBewerbRepository() }
        serviceLocator.register<DomLizenzRepository> { PostgresDomLizenzRepository() }
        serviceLocator.register<DomPferdRepository> { PostgresDomPferdRepository() }
        serviceLocator.register<DomQualifikationRepository> { PostgresDomQualifikationRepository() }
        serviceLocator.register<TurnierRepository> { PostgresTurnierRepository() }
        serviceLocator.register<VeranstaltungRepository> { PostgresVeranstaltungRepository() }
    }

    /**
     * Register all service implementations
     */
    private fun registerServices(serviceLocator: at.mocode.di.ServiceLocator) {
        // Register services with their dependencies
        serviceLocator.register<PersonService> {
            PersonService(serviceLocator.resolve<PersonRepository>())
        }
        serviceLocator.register<PlatzService> {
            PlatzService(serviceLocator.resolve<PlatzRepository>())
        }
        serviceLocator.register<VereinService> {
            VereinService(serviceLocator.resolve<VereinRepository>())
        }
        serviceLocator.register<ArtikelService> {
            ArtikelService(serviceLocator.resolve<ArtikelRepository>())
        }
        serviceLocator.register<AbteilungService> {
            AbteilungService(serviceLocator.resolve<AbteilungRepository>())
        }
        serviceLocator.register<BewerbService> {
            BewerbService(serviceLocator.resolve<BewerbRepository>())
        }
        serviceLocator.register<DomLizenzService> {
            DomLizenzService(serviceLocator.resolve<DomLizenzRepository>())
        }
        serviceLocator.register<DomPferdService> {
            DomPferdService(serviceLocator.resolve<DomPferdRepository>())
        }
        serviceLocator.register<DomQualifikationService> {
            DomQualifikationService(serviceLocator.resolve<DomQualifikationRepository>())
        }
        serviceLocator.register<TurnierService> {
            TurnierService(serviceLocator.resolve<TurnierRepository>())
        }
        serviceLocator.register<VeranstaltungService> {
            VeranstaltungService(serviceLocator.resolve<VeranstaltungRepository>())
        }
    }

    /**
     * Clear all registered services (useful for testing)
     */
    fun clearServices() {
        ServiceRegistry.serviceLocator.clear()
    }
}
