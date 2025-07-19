package at.mocode.members.domain.service

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomUser
import at.mocode.shared.config.AppConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.benasher44.uuid.Uuid
import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant

/**
 * Service für die Erstellung und Validierung von JWT-Tokens.
 */
actual class JwtService(private val userAuthorizationService: UserAuthorizationService) {

    // JWT-Konfiguration aus der Anwendungskonfiguration
    private val jwtConfig = AppConfig.security.jwt

    // HMAC-Algorithmus mit dem konfigurierten Secret
    private val algorithm = Algorithm.HMAC512(jwtConfig.secret)

    /**
     * Erstellt ein JWT-Token für einen Benutzer.
     *
     * @param user Der Benutzer, für den das Token erstellt werden soll
     * @return Das erstellte JWT-Token
     */
    actual suspend fun createToken(user: DomUser): String {
        // Berechtigungen des Benutzers ermitteln
        val permissions = userAuthorizationService.getUserPermissions(user.personId)

        // Aktuelle Zeit und Ablaufzeit berechnen
        val now = Clock.System.now()
        val expiryTime = now.plus(kotlin.time.Duration.parse("${jwtConfig.expirationInMinutes}m"))

        // Token erstellen
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withIssuedAt(Date.from(now.toJavaInstant()))
            .withExpiresAt(Date.from(expiryTime.toJavaInstant()))
            .withSubject(user.userId.toString())
            .withClaim("username", user.username)
            .withClaim("personId", user.personId.toString())
            .withArrayClaim("permissions", permissions.map { it.name }.toTypedArray())
            .sign(algorithm)
    }

    /**
     * Validiert ein JWT-Token und extrahiert die enthaltenen Informationen.
     *
     * @param token Das zu validierende JWT-Token
     * @return Die im Token enthaltenen Informationen, oder null bei ungültigem Token
     */
    actual fun validateToken(token: String): TokenInfo? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(jwtConfig.issuer)
                .withAudience(jwtConfig.audience)
                .build()

            val jwt = verifier.verify(token)

            val userId = UUID.fromString(jwt.subject)
            val personId = UUID.fromString(jwt.getClaim("personId").asString())
            val username = jwt.getClaim("username").asString()
            val permissionStrings = jwt.getClaim("permissions").asList(String::class.java)
            val permissions = permissionStrings.mapNotNull { permString ->
                try {
                    BerechtigungE.valueOf(permString)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            TokenInfo(
                userId = Uuid.fromString(userId.toString()),
                personId = Uuid.fromString(personId.toString()),
                username = username,
                permissions = permissions,
                issuedAt = Instant.fromEpochMilliseconds(jwt.issuedAt.time),
                expiresAt = Instant.fromEpochMilliseconds(jwt.expiresAt.time)
            )
        } catch (e: Exception) {
            null
        }
    }
}
