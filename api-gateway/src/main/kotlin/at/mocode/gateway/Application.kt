package at.mocode.gateway

import at.mocode.gateway.config.configureDatabase
import at.mocode.gateway.config.configureSerialization
import at.mocode.gateway.config.configureMonitoring
import at.mocode.gateway.config.configureSecurity
import at.mocode.gateway.config.configureOpenApi
import at.mocode.gateway.config.configureSwagger
import at.mocode.gateway.routing.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Main application entry point for the Self-Contained Systems API Gateway.
 *
 * This gateway aggregates all bounded context APIs into a unified interface
 * while maintaining the independence of each context.
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Main application module configuration.
 *
 * Configures all necessary components for the API Gateway including:
 * - Database connections for all contexts
 * - Serialization and content negotiation
 * - Security and authentication
 * - Monitoring and logging
 * - Route aggregation from all bounded contexts
 */
fun Application.module() {
    // Configure core components
    configureDatabase()
    configureSerialization()
    configureMonitoring()
    configureSecurity()

    // Configure API documentation
    configureOpenApi()
    configureSwagger()

    // Configure routing - aggregates all bounded context routes
    configureRouting()
}
