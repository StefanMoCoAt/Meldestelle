package at.mocode.members.domain.service

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomUser
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service für die Erstellung und Validierung von JWT-Tokens.
 * JavaScript-Implementation mit einfacher JWT-Funktionalität.
 */
actual class JwtService(private val userAuthorizationService: UserAuthorizationService) {

    companion object {
        private const val SECRET = "default-js-secret-key-change-in-production"
        private const val ISSUER = "meldestelle-js"
        private const val AUDIENCE = "meldestelle-users"
        private const val EXPIRATION_MINUTES = 60
    }

    @Serializable
    private data class JwtHeader(
        val alg: String = "HS256",
        val typ: String = "JWT"
    )

    @Serializable
    private data class JwtPayload(
        val iss: String,
        val aud: String,
        val sub: String,
        val iat: Long,
        val exp: Long,
        val username: String,
        val personId: String,
        val permissions: List<String>
    )

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
        val expiryTime = now.plus(kotlin.time.Duration.parse("${EXPIRATION_MINUTES}m"))

        // Header erstellen
        val header = JwtHeader()
        val headerJson = Json.encodeToString(header)
        val headerBase64 = js("btoa(headerJson)") as String

        // Payload erstellen
        val payload = JwtPayload(
            iss = ISSUER,
            aud = AUDIENCE,
            sub = user.userId.toString(),
            iat = now.epochSeconds,
            exp = expiryTime.epochSeconds,
            username = user.username,
            personId = user.personId.toString(),
            permissions = permissions.map { it.name }
        )
        val payloadJson = Json.encodeToString(payload)
        val payloadBase64 = js("btoa(payloadJson)") as String

        // Signatur erstellen (vereinfacht für JS)
        val message = "$headerBase64.$payloadBase64"
        val signature = createSignature(message, SECRET)

        return "$message.$signature"
    }

    /**
     * Validiert ein JWT-Token und extrahiert die enthaltenen Informationen.
     *
     * @param token Das zu validierende JWT-Token
     * @return Die im Token enthaltenen Informationen, oder null bei ungültigem Token
     */
    actual fun validateToken(token: String): TokenInfo? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val headerBase64 = parts[0]
            val payloadBase64 = parts[1]
            val signature = parts[2]

            // Signatur überprüfen
            val message = "$headerBase64.$payloadBase64"
            val expectedSignature = createSignature(message, SECRET)
            if (signature != expectedSignature) return null

            // Payload dekodieren
            val payloadJson = js("atob(payloadBase64)") as String
            val payload = Json.decodeFromString<JwtPayload>(payloadJson)

            // Ablaufzeit überprüfen
            val now = Clock.System.now()
            if (now.epochSeconds > payload.exp) return null

            // Berechtigungen konvertieren
            val permissions = payload.permissions.mapNotNull { permString ->
                try {
                    BerechtigungE.valueOf(permString)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            TokenInfo(
                userId = parseUuidFromString(payload.sub),
                personId = parseUuidFromString(payload.personId),
                username = payload.username,
                permissions = permissions,
                issuedAt = Instant.fromEpochSeconds(payload.iat),
                expiresAt = Instant.fromEpochSeconds(payload.exp)
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Erstellt eine einfache Signatur für das JWT-Token.
     * Dies ist eine vereinfachte Implementation für JS.
     */
    private fun createSignature(message: String, secret: String): String {
        val combined = message + secret
        var hash = 0
        for (i in combined.indices) {
            val char = combined[i].code
            hash = ((hash shl 5) - hash) + char
            hash = hash and hash // Convert to 32-bit integer
        }
        val hashString = hash.toString(16).padStart(8, '0')
        return js("btoa(hashString)") as String
    }

    /**
     * Parst einen UUID-String zu einem Uuid-Objekt.
     * Workaround für JS-Platform.
     */
    private fun parseUuidFromString(uuidString: String): Uuid {
        // Remove hyphens and convert to ByteArray
        val cleanUuid = uuidString.replace("-", "")
        val bytes = ByteArray(16)

        for (i in 0 until 16) {
            val hexPair = cleanUuid.substring(i * 2, i * 2 + 2)
            bytes[i] = hexPair.toInt(16).toByte()
        }

        return uuidOf(bytes)
    }
}
