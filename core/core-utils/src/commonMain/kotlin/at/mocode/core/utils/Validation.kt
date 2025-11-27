package at.mocode.core.utils

import at.mocode.core.domain.model.ValidationError

/**
 * Umfassende Validierungs-Utilities für das gesamte System.
 * Stellt typsichere und wiederverwendbare Validierungslogik bereit.
 */

/**
 * Builder-Klasse für die Erstellung von Validierungsregeln.
 */
class ValidationBuilder {
  private val errors = mutableListOf<ValidationError>()

  /**
   * Validiert ein Feld gegen mehrere Regeln.
   */
  fun <T> field(name: String, value: T, vararg rules: ValidationRule<T>): ValidationBuilder {
    rules.forEach { rule ->
      rule.validate(name, value)?.let { error ->
        errors.add(error)
      }
    }
    return this
  }

  /**
   * Fügt einen benutzerdefinierten Validierungsfehler hinzu.
   */
  fun addError(field: String, message: String, code: String = "VALIDATION_ERROR"): ValidationBuilder {
    errors.add(ValidationError(field, message, code))
    return this
  }

  /**
   * Führt eine benutzerdefinierten Validierung aus.
   */
  fun custom(validation: () -> ValidationError?): ValidationBuilder {
    validation()?.let { error ->
      errors.add(error)
    }
    return this
  }

  /**
   * Erstellt das finale Validierungsergebnis.
   */
  fun build(): Result<Unit> {
    return if (errors.isEmpty()) {
      Result.success(Unit)
    } else {
      Result.failure(errors)
    }
  }

  /**
   * Gibt die gesammelten Fehler zurück.
   */
  fun getErrors(): List<ValidationError> = errors.toList()
}

/**
 * Interface für Validierungsregeln.
 */
fun interface ValidationRule<T> {
  /**
   * Validiert einen Wert und gibt einen Fehler zurück, wenn die Validierung fehlschlägt.
   */
  fun validate(fieldName: String, value: T): ValidationError?
}

/**
 * Vordefinierte Validierungsregeln.
 */
object ValidationRules {

  // === String-Validierungen ===

  /**
   * Prüft ob ein String nicht leer ist.
   */
  fun notBlank(): ValidationRule<String> = ValidationRule { fieldName, value ->
    if (value.isBlank()) ValidationError.required(fieldName) else null
  }

  /**
   * Prüft die Mindestlänge eines Strings.
   */
  fun minLength(min: Int): ValidationRule<String> = ValidationRule { fieldName, value ->
    if (value.length < min) {
      ValidationError.invalidLength(fieldName, "$fieldName muss mindestens $min Zeichen lang sein")
    } else null
  }

  /**
   * Prüft die Maximallänge eines Strings.
   */
  fun maxLength(max: Int): ValidationRule<String> = ValidationRule { fieldName, value ->
    if (value.length > max) {
      ValidationError.invalidLength(fieldName, "$fieldName darf $max Zeichen nicht überschreiten")
    } else null
  }

  /**
   * Prüft ob ein String einem RegEx-Pattern entspricht.
   */
  fun matches(pattern: Regex, message: String): ValidationRule<String> = ValidationRule { fieldName, value ->
    if (!value.matches(pattern)) {
      ValidationError.invalidFormat(fieldName, message)
    } else null
  }

  /**
   * Prüft ob ein String eine gültige E-Mail-Adresse ist.
   */
  fun email(): ValidationRule<String> = ValidationRule { fieldName, value ->
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!value.matches(emailRegex)) {
      ValidationError.invalidFormat(fieldName, "$fieldName muss eine gültige E-Mail-Adresse sein")
    } else null
  }

  // === Numerische Validierungen ===

  /**
   * Prüft den Mindestwert einer Zahl.
   */
  fun <T : Comparable<T>> min(minValue: T): ValidationRule<T> = ValidationRule { fieldName, value ->
    if (value < minValue) {
      ValidationError.invalidRange(fieldName, "$fieldName muss mindestens $minValue sein")
    } else null
  }

  /**
   * Prüft den Maximalwert einer Zahl.
   */
  fun <T : Comparable<T>> max(maxValue: T): ValidationRule<T> = ValidationRule { fieldName, value ->
    if (value > maxValue) {
      ValidationError.invalidRange(fieldName, "$fieldName darf $maxValue nicht überschreiten")
    } else null
  }

  /**
   * Prüft ob eine Zahl positiv ist.
   */
  fun positive(): ValidationRule<Number> = ValidationRule { fieldName, value ->
    if (value.toDouble() <= 0) {
      ValidationError.invalidRange(fieldName, "$fieldName muss positiv sein")
    } else null
  }

  /**
   * Prüft ob eine Zahl nicht negativ ist.
   */
  fun nonNegative(): ValidationRule<Number> = ValidationRule { fieldName, value ->
    if (value.toDouble() < 0) {
      ValidationError.invalidRange(fieldName, "$fieldName darf nicht negativ sein")
    } else null
  }

  // === Collection-Validierungen ===

  /**
   * Prüft ob eine Collection nicht leer ist.
   */
  fun <T> notEmpty(): ValidationRule<Collection<T>> = ValidationRule { fieldName, value ->
    if (value.isEmpty()) {
      ValidationError.required(fieldName)
    } else null
  }

  /**
   * Prüft die Mindestgröße einer Collection.
   */
  fun <T> minSize(min: Int): ValidationRule<Collection<T>> = ValidationRule { fieldName, value ->
    if (value.size < min) {
      ValidationError.invalidLength(fieldName, "$fieldName muss mindestens $min Elemente enthalten")
    } else null
  }

  /**
   * Prüft die Maximalgröße einer Collection.
   */
  fun <T> maxSize(max: Int): ValidationRule<Collection<T>> = ValidationRule { fieldName, value ->
    if (value.size > max) {
      ValidationError.invalidLength(fieldName, "$fieldName darf nicht mehr als $max Elemente enthalten")
    } else null
  }

  // === Null-Validierungen ===

  /**
   * Prüft ob ein Wert nicht null ist.
   */
  fun <T> notNull(): ValidationRule<T?> = ValidationRule { fieldName, value ->
    if (value == null) ValidationError.required(fieldName) else null
  }
}

/**
 * DSL-Funktion für die Erstellung von Validierungen.
 */
inline fun validate(builder: ValidationBuilder.() -> Unit): Result<Unit> {
  return ValidationBuilder().apply(builder).build()
}

/**
 * Extension-Funktion für einfache String-Validierung.
 */
fun String?.validateNotBlank(fieldName: String): ValidationError? {
  return if (this.isNullOrBlank()) ValidationError.required(fieldName) else null
}

/**
 * Extension-Funktion für einfache E-Mail-Validierung.
 */
fun String?.validateEmail(fieldName: String): ValidationError? {
  if (this.isNullOrBlank()) return ValidationError.required(fieldName)
  val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
  return if (!this.matches(emailRegex)) {
    ValidationError.invalidFormat(fieldName, "$fieldName muss eine gültige E-Mail-Adresse sein")
  } else null
}
