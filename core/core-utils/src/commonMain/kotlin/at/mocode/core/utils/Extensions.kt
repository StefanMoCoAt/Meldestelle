@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.core.utils

import at.mocode.core.domain.model.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Extension-Funktionen für häufig verwendete Operationen im gesamten System.
 */

// === UUID Generation Extensions ===

/**
 * Erstellt eine neue EntityId mit einer zufälligen UUID.
 */
fun EntityId.Companion.random(): EntityId = EntityId(Uuid.random())

/**
 * Erstellt eine neue EventId mit einer zufälligen UUID.
 */
fun EventId.Companion.random(): EventId = EventId(Uuid.random())

/**
 * Erstellt eine neue AggregateId mit einer zufälligen UUID.
 */
fun AggregateId.Companion.random(): AggregateId = AggregateId(Uuid.random())

/**
 * Erstellt eine neue CorrelationId mit einer zufälligen UUID.
 */
fun CorrelationId.Companion.random(): CorrelationId = CorrelationId(Uuid.random())

/**
 * Erstellt eine neue CausationId mit einer zufälligen UUID.
 */
fun CausationId.Companion.random(): CausationId = CausationId(Uuid.random())

// === String Extensions ===

/**
 * Konvertiert einen String zu einem EventType mit Validierung.
 */
fun String.toEventType(): EventType = EventType(this)

/**
 * Konvertiert einen String zu einem ErrorCode mit Validierung.
 */
fun String.toErrorCode(): ErrorCode = ErrorCode(this)

/**
 * Prüft ob der String ein gültiger EventType-Name ist.
 */
fun String.isValidEventType(): Boolean {
    return isNotBlank() && matches(Regex("^[A-Za-z][A-Za-z0-9]*$"))
}

/**
 * Prüft ob der String ein gültiger ErrorCode ist.
 */
fun String.isValidErrorCode(): Boolean {
    return isNotBlank() && matches(Regex("^[A-Z][A-Z0-9_]*$"))
}

// === Collection Extensions ===

/**
 * Erstellt eine PagedResponse aus einer Liste mit Standard-Paginierung.
 */
fun <T> List<T>.toPagedResponse(
    page: Int = 0,
    size: Int = 20
): PagedResponse<T> {
    val startIndex = page * size
    val endIndex = minOf(startIndex + size, this.size)
    val content = if (startIndex < this.size) this.subList(startIndex, endIndex) else emptyList()

    return PagedResponse.create(
        content = content,
        page = page,
        size = size,
        totalElements = this.size.toLong(),
        totalPages = (this.size + size - 1) / size,
        hasNext = endIndex < this.size,
        hasPrevious = page > 0
    )
}

// === Validation Extensions ===

/**
 * Erstellt eine Liste von ValidationError aus einer Map von Fehlern.
 */
fun Map<String, String>.toValidationErrors(): List<ValidationError> {
    return this.map { (field, message) -> ValidationError(field, message, "VALIDATION_ERROR") }
}

/**
 * Prüft ob eine Liste von ValidationError leer ist.
 */
fun List<ValidationError>.hasErrors(): Boolean = this.isNotEmpty()

/**
 * Konvertiert eine Liste von ValidationError zu ErrorDto.
 */
fun List<ValidationError>.toErrorDtos(): List<ErrorDto> {
    return this.map { ErrorDto(ErrorCode(it.code), it.message, it.field) }
}

// === Time Extensions ===

/**
 * Prüft ob ein Zeitstempel in der Vergangenheit liegt.
 */
@OptIn(ExperimentalTime::class)
fun Instant.isPast(): Boolean = this < Clock.System.now()

/**
 * Prüft ob ein Zeitstempel in der Zukunft liegt.
 */
@OptIn(ExperimentalTime::class)
fun Instant.isFuture(): Boolean = this > Clock.System.now()
