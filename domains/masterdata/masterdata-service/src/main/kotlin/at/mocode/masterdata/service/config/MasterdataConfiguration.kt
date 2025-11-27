package at.mocode.masterdata.service.config

import at.mocode.masterdata.application.usecase.*
import at.mocode.masterdata.domain.repository.*
import at.mocode.masterdata.infrastructure.persistence.*
import at.mocode.masterdata.api.rest.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Spring Boot configuration for the Masterdata Service.
 *
 * This configuration class sets up all the necessary beans for dependency injection
 * following the clean architecture pattern with proper separation of concerns.
 */
@Configuration
class MasterdataConfiguration {

    // Repository Implementations
    @Bean
    fun landRepository(): LandRepository {
        return LandRepositoryImpl()
    }

    @Bean
    fun bundeslandRepository(): BundeslandRepository {
        return BundeslandRepositoryImpl()
    }

    @Bean
    fun altersklasseRepository(): AltersklasseRepository {
        return AltersklasseRepositoryImpl()
    }

    @Bean
    fun platzRepository(): PlatzRepository {
        return PlatzRepositoryImpl()
    }

    // Use Cases - Country/Land
    @Bean
    fun getCountryUseCase(landRepository: LandRepository): GetCountryUseCase {
        return GetCountryUseCase(landRepository)
    }

    @Bean
    fun createCountryUseCase(landRepository: LandRepository): CreateCountryUseCase {
        return CreateCountryUseCase(landRepository)
    }

    // Use Cases - Federal State/Bundesland
    @Bean
    fun getBundeslandUseCase(bundeslandRepository: BundeslandRepository): GetBundeslandUseCase {
        return GetBundeslandUseCase(bundeslandRepository)
    }

    @Bean
    fun createBundeslandUseCase(bundeslandRepository: BundeslandRepository): CreateBundeslandUseCase {
        return CreateBundeslandUseCase(bundeslandRepository)
    }

    // Use Cases - Age Class/Altersklasse
    @Bean
    fun getAltersklasseUseCase(altersklasseRepository: AltersklasseRepository): GetAltersklasseUseCase {
        return GetAltersklasseUseCase(altersklasseRepository)
    }

    @Bean
    fun createAltersklasseUseCase(altersklasseRepository: AltersklasseRepository): CreateAltersklasseUseCase {
        return CreateAltersklasseUseCase(altersklasseRepository)
    }

    // Use Cases - Venue/Platz
    @Bean
    fun getPlatzUseCase(platzRepository: PlatzRepository): GetPlatzUseCase {
        return GetPlatzUseCase(platzRepository)
    }

    @Bean
    fun createPlatzUseCase(platzRepository: PlatzRepository): CreatePlatzUseCase {
        return CreatePlatzUseCase(platzRepository)
    }

    // API Controllers
    @Bean
    fun countryController(
        getCountryUseCase: GetCountryUseCase,
        createCountryUseCase: CreateCountryUseCase
    ): CountryController {
        return CountryController(getCountryUseCase, createCountryUseCase)
    }

    @Bean
    fun bundeslandController(
        getBundeslandUseCase: GetBundeslandUseCase,
        createBundeslandUseCase: CreateBundeslandUseCase
    ): BundeslandController {
        return BundeslandController(getBundeslandUseCase, createBundeslandUseCase)
    }

    @Bean
    fun altersklasseController(
        getAltersklasseUseCase: GetAltersklasseUseCase,
        createAltersklasseUseCase: CreateAltersklasseUseCase
    ): AltersklasseController {
        return AltersklasseController(getAltersklasseUseCase, createAltersklasseUseCase)
    }

    @Bean
    fun platzController(
        getPlatzUseCase: GetPlatzUseCase,
        createPlatzUseCase: CreatePlatzUseCase
    ): PlatzController {
        return PlatzController(getPlatzUseCase, createPlatzUseCase)
    }
}

/**
 * Database configuration for different environments.
 */
@Configuration
class DatabaseConfiguration {

    /**
     * Development database configuration.
     */
    @Configuration
    @Profile("dev", "development")
    class DevelopmentDatabaseConfig {
        // Development-specific database configuration
        // This would typically include H2 or local PostgreSQL setup
    }

    /**
     * Production database configuration.
     */
    @Configuration
    @Profile("prod", "production")
    class ProductionDatabaseConfig {
        // Production-specific database configuration
        // This would include production PostgreSQL setup with connection pooling
    }

    /**
     * Test database configuration.
     */
    @Configuration
    @Profile("test")
    class TestDatabaseConfig {
        // Test-specific database configuration
        // This would typically include in-memory H2 database
    }
}
