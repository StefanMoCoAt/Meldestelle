package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Marker-Interface für alle Data-Transfer-Objekte (DTO).
 */
interface BaseDto

/**
 * Basis-DTO für Domänen-Entitäten mit eindeutiger ID und Audit-Zeitstempeln.
 */
@Serializable
@OptIn(ExperimentalTime::class)
abstract class EntityDto : BaseDto {
    abstract val id: EntityId

    @Serializable(with = KotlinInstantSerializer::class)
    abstract val createdAt: Instant

    @Serializable(with = KotlinInstantSerializer::class)
    abstract val updatedAt: Instant
}

/**
 * Strukturierte Darstellung eines einzelnen Fehlers (Code, Nachricht, optionales Feld).
 */
@Serializable
data class ErrorDto(
    val code: ErrorCode,
    val message: String,
    val field: String? = null
) : BaseDto

/**
 * Standardisierte Hülle für API-Antworten mit einheitlicher Struktur.
 */
@Serializable
@OptIn(ExperimentalTime::class)
data class ApiResponse<T>(
    val data: T?,
    val success: Boolean,
    val errors: List<ErrorDto> = emptyList(),
    @Serializable(with = KotlinInstantSerializer::class)
    val timestamp: Instant
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(data = data, success = true, timestamp = Clock.System.now())
        }

        @OptIn(ExperimentalTime::class)
        fun <T> error(
            code: ErrorCode,
            message: String,
            field: String? = null
        ): ApiResponse<T> {
            return ApiResponse(
                data = null,
                success = false,
                errors = listOf(ErrorDto(code = code, message = message, field = field)),
                timestamp = Clock.System.now()
            )
        }

        @OptIn(ExperimentalTime::class)
        fun <T> error(
            code: String,
            message: String,
            field: String? = null
        ): ApiResponse<T> {
            return error(ErrorCode(code), message, field)
        }

        @OptIn(ExperimentalTime::class)
        fun <T> error(errors: List<ErrorDto>): ApiResponse<T> {
            return ApiResponse(data = null, success = false, errors = errors, timestamp = Clock.System.now())
        }
    }
}

/**
 * Standardisierte Hülle für paginierte API-Antworten.
 */
@Serializable
data class PagedResponse<T>(
    val content: List<T>,
    val page: PageNumber,
    val size: PageSize,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        /**
         * Erzeugt eine PagedResponse mit Rückwärtskompatibilität für einfache Int-Werte.
         * Nützlich, wenn Aufrufer noch keine PageNumber/PageSize verwenden.
         */
        fun <T> create(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long,
            totalPages: Int,
            hasNext: Boolean,
            hasPrevious: Boolean
        ): PagedResponse<T> {
            return PagedResponse(
                content = content,
                page = PageNumber(page),
                size = PageSize(size),
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = hasNext,
                hasPrevious = hasPrevious
            )
        }
    }
}
