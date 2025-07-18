package at.mocode.gateway.routing

import at.mocode.dto.base.BaseDto
import at.mocode.horses.infrastructure.api.HorseController
import at.mocode.horses.infrastructure.repository.HorseRepositoryImpl
import at.mocode.masterdata.application.usecase.CreateCountryUseCase
import at.mocode.masterdata.application.usecase.GetCountryUseCase
import at.mocode.masterdata.infrastructure.api.CountryController
import at.mocode.masterdata.infrastructure.repository.LandRepositoryImpl
import at.mocode.members.domain.service.AuthenticationService
import at.mocode.members.domain.service.JwtService
import at.mocode.members.domain.service.UserAuthorizationService
import at.mocode.members.domain.service.PasswordService
import at.mocode.members.infrastructure.repository.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Main routing configuration for the API Gateway.
 *
 * This aggregates routes from all bounded contexts into a unified API
 * while maintaining the independence and self-contained nature of each context.
 */
fun Application.configureRouting() {

    // Initialize repository implementations for each context
    val landRepository = LandRepositoryImpl()
    val horseRepository = HorseRepositoryImpl()

    // Initialize authentication repositories
    val userRepository = UserRepositoryImpl()
    val personRolleRepository = PersonRolleRepositoryImpl()
    val rolleRepository = RolleRepositoryImpl()
    val rolleBerechtigungRepository = RolleBerechtigungRepositoryImpl()
    val berechtigungRepository = BerechtigungRepositoryImpl()

    // Initialize authentication services
    val passwordService = PasswordService()
    val userAuthorizationService = UserAuthorizationService(
        userRepository,
        personRolleRepository,
        rolleRepository,
        rolleBerechtigungRepository,
        berechtigungRepository
    )
    val jwtService = JwtService(userAuthorizationService)
    val authenticationService = AuthenticationService(
        userRepository,
        passwordService,
        jwtService
    )

    // Initialize use cases
    val getCountryUseCase = GetCountryUseCase(landRepository)
    val createCountryUseCase = CreateCountryUseCase(landRepository)

    // Initialize controllers for each bounded context
    val countryController = CountryController(getCountryUseCase, createCountryUseCase)
    val horseController = HorseController(horseRepository)

    routing {

        // Root endpoint - API Gateway health check and info
        get("/") {
            call.respond(HttpStatusCode.OK, BaseDto.success(
                ApiGatewayInfo(
                    name = "Meldestelle API Gateway",
                    version = "1.0.0",
                    description = "Self-Contained Systems API Gateway for Austrian Equestrian Federation",
                    availableContexts = listOf(
                        "authentication",
                        "master-data",
                        "horse-registry"
                    ),
                    endpoints = mapOf(
                        "authentication" to "/auth/*",
                        "master-data" to "/api/masterdata/*",
                        "horse-registry" to "/api/horses/*"
                    )
                )
            ))
        }

        // Health check endpoint
        get("/health") {
            call.respond(HttpStatusCode.OK, BaseDto.success(
                HealthStatus(
                    status = "UP",
                    contexts = mapOf(
                        "authentication" to "UP",
                        "master-data" to "UP",
                        "horse-registry" to "UP"
                    )
                )
            ))
        }

        // API documentation endpoint
        get("/api") {
            call.respond(HttpStatusCode.OK, BaseDto.success(
                ApiDocumentation(
                    title = "Meldestelle Self-Contained Systems API",
                    description = "Unified API Gateway for all bounded contexts",
                    contexts = listOf(
                        ContextInfo(
                            name = "Authentication Context",
                            path = "/auth",
                            description = "User authentication, registration, and profile management"
                        ),
                        ContextInfo(
                            name = "Master Data Context",
                            path = "/api/masterdata",
                            description = "Reference data management (countries, states, age classes, venues)"
                        ),
                        ContextInfo(
                            name = "Horse Registry Context",
                            path = "/api/horses",
                            description = "Horse registration, ownership, and pedigree management"
                        )
                    )
                )
            ))
        }

        // Configure routes for each bounded context

        // Authentication Routes
        authRoutes(authenticationService, jwtService)

        // Master Data Context Routes
        countryController.configureRoutes(this)

        // Horse Registry Context Routes
        horseController.configureRoutes(this)

        // Catch-all for undefined routes
        route("{...}") {
            handle {
                call.respond(
                    HttpStatusCode.NotFound,
                    BaseDto.error<Any>("Endpoint not found. Check /api for available endpoints.")
                )
            }
        }
    }
}

/**
 * API Gateway information DTO.
 */
@Serializable
data class ApiGatewayInfo(
    val name: String,
    val version: String,
    val description: String,
    val availableContexts: List<String>,
    val endpoints: Map<String, String>
)

/**
 * Health status DTO.
 */
@Serializable
data class HealthStatus(
    val status: String,
    val contexts: Map<String, String>
)

/**
 * API documentation DTO.
 */
@Serializable
data class ApiDocumentation(
    val title: String,
    val description: String,
    val contexts: List<ContextInfo>
)

/**
 * Context information DTO.
 */
@Serializable
data class ContextInfo(
    val name: String,
    val path: String,
    val description: String
)
