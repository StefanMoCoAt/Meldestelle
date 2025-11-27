@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.UuidSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * Value-Classes für stark typisierte IDs und Fachwerte.
 * Bieten Typsicherheit zur Compile-Zeit ohne Laufzeit-Overhead.
 */

// === ID Value Classes ===

/**
 * Stark typisierte Hülle für Entitäts-IDs.
 */
@Serializable
@JvmInline
value class EntityId(@Serializable(with = UuidSerializer::class) val value: Uuid) {
  companion object
}

/**
 * A strongly typed wrapper for event IDs.
 */
@Serializable
@JvmInline
value class EventId(@Serializable(with = UuidSerializer::class) val value: Uuid) {
  companion object
}

/**
 * A strongly typed wrapper for aggregate IDs.
 */
@Serializable
@JvmInline
value class AggregateId(@Serializable(with = UuidSerializer::class) val value: Uuid) {
  companion object
}

/**
 * A strongly typed wrapper for correlation IDs used in event tracing.
 */
@Serializable
@JvmInline
value class CorrelationId(@Serializable(with = UuidSerializer::class) val value: Uuid) {
  companion object
}

/**
 * A strongly typed wrapper for causation IDs used in event tracing.
 */
@Serializable
@JvmInline
value class CausationId(@Serializable(with = UuidSerializer::class) val value: Uuid) {
  companion object
}

// === Domain Value Classes ===

/**
 * A strongly typed wrapper for event types.
 */
@Serializable
@JvmInline
value class EventType(val value: String) {
  init {
    require(value.isNotBlank()) { "Event type cannot be blank" }
    require(value.matches(Regex("^[A-Za-z][A-Za-z0-9]*$"))) {
      "Event type must start with a letter and contain only alphanumeric characters"
    }
  }

  override fun toString(): String = value
}

/**
 * A strongly typed wrapper for event version numbers.
 */
@Serializable
@JvmInline
value class EventVersion(val value: Long) : Comparable<EventVersion> {
  init {
    require(value >= 0) { "Event version must be non-negative" }
  }

  override fun toString(): String = value.toString()

  override fun compareTo(other: EventVersion): Int = value.compareTo(other.value)
}

/**
 * A strongly typed wrapper for error codes.
 */
@Serializable
@JvmInline
value class ErrorCode(val value: String) {
  init {
    require(value.isNotBlank()) { "Error code cannot be blank" }
    require(value.matches(Regex("^[A-Z][A-Z0-9_]*$"))) {
      "Error code must be uppercase and contain only letters, numbers, and underscores"
    }
  }

  override fun toString(): String = value
}

/**
 * A strongly typed wrapper for page numbers in pagination.
 */
@Serializable
@JvmInline
value class PageNumber(val value: Int) {
  init {
    require(value >= 0) { "Page number must be non-negative" }
  }

  override fun toString(): String = value.toString()
}

/**
 * A strongly typed wrapper for page sizes in pagination.
 */
@Serializable
@JvmInline
value class PageSize(val value: Int) {
  init {
    require(value > 0) { "Page size must be positive" }
    require(value <= 1000) { "Page size cannot exceed 1000" }
  }

  override fun toString(): String = value.toString()
}
