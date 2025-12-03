@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package at.mocode.infrastructure.eventstore.api

import at.mocode.core.domain.event.DomainEvent
import kotlin.uuid.Uuid

/**
 * Schnittstelle für die Serialisierung und Deserialisierung von Domain-Events.
 */
interface EventSerializer {
    /**
     * Serialisiert ein Domain-Event zu einer Map von Strings zu Strings.
     * Dieses Format ist für die Speicherung in Redis Streams geeignet.
     *
     * @param event Das zu serialisierende Event
     * @return Eine Map von Strings zu Strings, die das Event repräsentiert
     */
    fun serialize(event: DomainEvent): Map<String, String>

    /**
     * Deserialisiert eine Map von Strings zu einem Domain-Event.
     *
     * @param data Die zu deserialisierende Map von Strings
     * @return Das deserialisierte Domain-Event
     */
    fun deserialize(data: Map<String, String>): DomainEvent

    /**
     * Ermittelt den Typ des Domain-Events.
     * Dies wird verwendet, um den Typ des Events bei der Deserialisierung zu bestimmen.
     *
     * @param event Das Event, dessen Typ ermittelt werden soll
     * @return Der Typ des Events als String
     */
    fun getEventType(event: DomainEvent): String

    /**
     * Ermittelt den Typ des Domain-Events aus einer serialisierten Map.
     *
     * @param data Die serialisierten Event-Daten
     * @return Der Typ des Events als String
     */
    fun getEventType(data: Map<String, String>): String

    /**
     * Registriert eine Domain-Event-Klasse beim Serializer.
     * Dies wird verwendet, um Event-Typen auf ihre entsprechenden Klassen abzubilden.
     *
     * @param eventClass Die Klasse des zu registrierenden Events
     * @param eventType Der Typ des Events als String
     */
    fun registerEventType(eventClass: Class<out DomainEvent>, eventType: String)

    /**
     * Ermittelt die Aggregat-ID aus einem serialisierten Event.
     *
     * @param data Die serialisierten Event-Daten
     * @return Die Aggregat-ID
     */
    fun getAggregateId(data: Map<String, String>): Uuid

    /**
     * Ermittelt die Event-ID aus einem serialisierten Event.
     *
     * @param data Die serialisierten Event-Daten
     * @return Die Event-ID
     */
    fun getEventId(data: Map<String, String>): Uuid

    /**
     * Ermittelt die Version aus einem serialisierten Event.
     *
     * @param data Die serialisierten Event-Daten
     * @return Die Version
     */
    fun getVersion(data: Map<String, String>): Long
}
