package at.mocode.infrastructure.auth.config

import org.springframework.context.annotation.Configuration

/**
 * Spring-Konfiguration für das Auth-Server-Modul.
 *
 * Note: JWT handling is now fully delegated to Keycloak via OAuth2 Resource Server.
 * This auth-server focuses on user management through Keycloak Admin Client.
 */
@Configuration
class AuthServerConfiguration
