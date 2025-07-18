package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

/**
 * Security configuration for the API Gateway.
 *
 * Configures CORS, JWT authentication, and other security-related settings.
 */
fun Application.configureSecurity() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Requested-With")

        // Allow requests from common development origins
        allowHost("localhost:3000")
        allowHost("localhost:8080")
        allowHost("127.0.0.1:3000")
        allowHost("127.0.0.1:8080")

        // In production, configure specific allowed origins
        anyHost() // This should be restricted in production
    }

    // JWT Configuration
    val jwtConfig = JwtConfig.fromEnvironment()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

/**
 * JWT Configuration data class.
 */
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationTime: Long = 3600000L // 1 hour in milliseconds
) {
    companion object {
        fun fromEnvironment(): JwtConfig {
            return JwtConfig(
                secret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production",
                issuer = System.getenv("JWT_ISSUER") ?: "meldestelle-api",
                audience = System.getenv("JWT_AUDIENCE") ?: "meldestelle-users",
                realm = System.getenv("JWT_REALM") ?: "Meldestelle API",
                expirationTime = System.getenv("JWT_EXPIRATION")?.toLongOrNull() ?: 3600000L
            )
        }
    }
}
