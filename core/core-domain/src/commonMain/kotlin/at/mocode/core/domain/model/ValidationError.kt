package at.mocode.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Validierungsfehler mit Feldname, Nachricht und Fehlercode.
 * Wird von Validierungs-Hilfsfunktionen im gesamten System verwendet.
 */
@Serializable
data class ValidationError(
    val field: String,
    val message: String,
    val code: String
) : BaseDto {

    companion object {
        /**
         * Erzeugt einen Validierungsfehler für Pflichtfeld-Prüfungen.
         */
        fun required(field: String): ValidationError {
            return ValidationError(field, "$field ist erforderlich", "REQUIRED")
        }

        /**
         * Erzeugt einen Validierungsfehler für ungültiges Format.
         */
        fun invalidFormat(field: String, message: String = "Ungültiges Format"): ValidationError {
            return ValidationError(field, message, "INVALID_FORMAT")
        }

        /**
         * Erzeugt einen Validierungsfehler für Längenprüfungen.
         */
        fun invalidLength(field: String, message: String): ValidationError {
            return ValidationError(field, message, "INVALID_LENGTH")
        }

        /**
         * Erzeugt einen Validierungsfehler für Bereichsprüfungen.
         */
        fun invalidRange(field: String, message: String): ValidationError {
            return ValidationError(field, message, "INVALID_RANGE")
        }
    }
}
