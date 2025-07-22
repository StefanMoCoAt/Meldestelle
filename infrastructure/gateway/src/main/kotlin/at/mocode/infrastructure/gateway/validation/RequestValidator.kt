package at.mocode.infrastructure.gateway.validation

import at.mocode.core.domain.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

/**
 * Klasse für die Validierung von API-Anfragen.
 * Bietet Methoden zum Validieren und Verarbeiten von Request-Daten.
 */
class RequestValidator {
    companion object {
        /**
         * Validiert und verarbeitet eine Anfrage.
         *
         * @param call Der ApplicationCall
         * @param validator Eine Funktion, die den Request validiert und eine Liste von Fehlern zurückgibt
         * @param processor Eine Funktion, die den validierten Request verarbeitet
         * @return true, wenn die Validierung erfolgreich war, false sonst
         */
        suspend inline fun <reified T : Any> validateAndProcess(
            call: ApplicationCall,
            crossinline validator: (T) -> List<String>,
            crossinline processor: suspend (T) -> Unit
        ): Boolean {
            try {
                // Request-Daten lesen
                val request = call.receive<T>()

                // Validierung durchführen
                val errors = validator(request)
                if (errors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<T>("Validierungsfehler")
                    )
                    return false
                }

                // Request verarbeiten
                processor(request)
                return true
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<T>("Fehler bei der Anfrageverarbeitung: ${e.message}")
                )
                return false
            }
        }

        /**
         * Validiert Pflichtfelder in einem Request.
         *
         * @param fields Map von Feldnamen zu Feldwerten
         * @return Liste von Fehlermeldungen für fehlende Pflichtfelder
         */
        fun validateRequiredFields(vararg fields: Pair<String, Any?>): List<String> {
            return fields
                .filter { (_, value) ->
                    when (value) {
                        null -> true
                        is String -> value.isBlank()
                        is Collection<*> -> value.isEmpty()
                        else -> false
                    }
                }
                .map { (name, _) -> "Das Feld '$name' ist erforderlich" }
        }

        /**
         * Validiert die Länge eines Textfeldes.
         *
         * @param name Name des Feldes
         * @param value Wert des Feldes
         * @param minLength Minimale Länge
         * @param maxLength Maximale Länge
         * @return Fehlermeldung, wenn die Länge ungültig ist, sonst null
         */
        fun validateStringLength(name: String, value: String?, minLength: Int, maxLength: Int): String? {
            if (value == null) return null

            return when {
                value.length < minLength -> "Das Feld '$name' muss mindestens $minLength Zeichen enthalten"
                value.length > maxLength -> "Das Feld '$name' darf höchstens $maxLength Zeichen enthalten"
                else -> null
            }
        }

        /**
         * Validiert eine E-Mail-Adresse.
         *
         * @param email Die zu validierende E-Mail-Adresse
         * @return true, wenn die E-Mail-Adresse gültig ist, false sonst
         */
        fun isValidEmail(email: String?): Boolean {
            if (email == null) return false
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            return email.matches(emailRegex.toRegex())
        }
    }
}
