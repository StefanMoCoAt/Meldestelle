package at.mocode.infrastructure.auth.config

import org.springframework.boot.test.context.TestConfiguration

/**
 * Test configuration for Auth Server integration tests.
 *
 * Note: Custom JWT handling has been removed. Authentication is now fully handled
 * by Keycloak via OAuth2 Resource Server. This configuration class is kept as a
 * placeholder for future test-specific beans if needed.
 */
@TestConfiguration
class AuthServerTestConfiguration
