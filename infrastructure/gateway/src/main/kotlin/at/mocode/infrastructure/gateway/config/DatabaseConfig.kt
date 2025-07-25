package at.mocode.infrastructure.gateway.config

/**
 * Database configuration for the API Gateway.
 *
 * The gateway uses DatabaseFactory.init() in Application.kt for proper connection pooling.
 * Schema initialization is handled by individual services in their @PostConstruct methods
 * to prevent race conditions and maintain proper separation of concerns.
 *
 * This file is kept for potential future gateway-specific database utilities.
 */
