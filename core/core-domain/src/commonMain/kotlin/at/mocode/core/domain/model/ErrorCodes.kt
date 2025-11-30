package at.mocode.core.domain.model

/**
 * Zentrale Sammlung der standardisierten Fehlercodes der Anwendung.
 * Dient als Single-Source-of-Truth, um Inkonsistenzen zu vermeiden.
 */
object ErrorCodes {
  val DUPLICATE_ENTRY = ErrorCode("DUPLICATE_ENTRY")
  val CONSTRAINT_VIOLATION = ErrorCode("CONSTRAINT_VIOLATION")
  val FOREIGN_KEY_VIOLATION = ErrorCode("FOREIGN_KEY_VIOLATION")
  val CHECK_VIOLATION = ErrorCode("CHECK_VIOLATION")
  val DATABASE_TIMEOUT = ErrorCode("DATABASE_TIMEOUT")
  val DATABASE_ERROR = ErrorCode("DATABASE_ERROR")
  val TRANSACTION_ERROR = ErrorCode("TRANSACTION_ERROR")
  val VALIDATION_ERROR = ErrorCode("VALIDATION_ERROR")
}
