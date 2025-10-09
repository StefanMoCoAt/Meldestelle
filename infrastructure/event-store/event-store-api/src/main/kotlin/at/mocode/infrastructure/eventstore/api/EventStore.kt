@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package at.mocode.infrastructure.eventstore.api

import at.mocode.core.domain.event.DomainEvent
import kotlin.uuid.Uuid

/**
 * Schnittstelle für einen Event Store, der Domain-Events persistiert.
 */
interface EventStore {
    /**
     * Fügt ein Event zum Event Store hinzu.
     *
     * @param event Das hinzuzufügende Event
     * @param streamId Die ID des Event-Streams (normalerweise die Aggregat-ID)
     * @param expectedVersion Die erwartete Version des Streams (für optimistische Nebenläufigkeitskontrolle)
     * @return Die neue Version des Streams
     * @throws ConcurrencyException wenn die erwartete Version nicht mit der tatsächlichen Version übereinstimmt
     */
    fun appendToStream(event: DomainEvent, streamId: Uuid, expectedVersion: Long): Long

    /**
     * Fügt mehrere Events zum Event Store hinzu.
     *
     * @param events Die hinzuzufügenden Events
     * @param streamId Die ID des Event-Streams (normalerweise die Aggregat-ID)
     * @param expectedVersion Die erwartete Version des Streams (für optimistische Nebenläufigkeitskontrolle)
     * @return Die neue Version des Streams
     * @throws ConcurrencyException wenn die erwartete Version nicht mit der tatsächlichen Version übereinstimmt
     */
    fun appendToStream(events: List<DomainEvent>, streamId: Uuid, expectedVersion: Long): Long

    /**
     * Liest Events aus einem Stream.
     *
     * @param streamId Die ID des Event-Streams, aus dem gelesen werden soll
     * @param fromVersion Die Version, ab der gelesen werden soll (inklusive)
     * @param toVersion Die Version, bis zu der gelesen werden soll (inklusive), oder null um alle Events zu lesen
     * @return Die Events im Stream
     */
    fun readFromStream(streamId: Uuid, fromVersion: Long = 0, toVersion: Long? = null): List<DomainEvent>

    /**
     * Liest alle Events aus allen Streams.
     *
     * @param fromPosition Die Position, ab der gelesen werden soll (inklusive)
     * @param maxCount Die maximale Anzahl der zu lesenden Events, oder null um alle Events zu lesen
     * @return Die Events in allen Streams
     */
    fun readAllEvents(fromPosition: Long = 0, maxCount: Int? = null): List<DomainEvent>

    /**
     * Ermittelt die aktuelle Version eines Streams.
     *
     * @param streamId Die ID des Event-Streams
     * @return Die aktuelle Version des Streams, oder -1 wenn der Stream nicht existiert
     */
    fun getStreamVersion(streamId: Uuid): Long

    /**
     * Abonniert Events von einem spezifischen Stream.
     *
     * @param streamId Die ID des Event-Streams, der abonniert werden soll
     * @param fromVersion Die Version, ab der abonniert werden soll (inklusive)
     * @param handler Der Handler, der für jedes Event aufgerufen wird
     * @return Ein Abonnement, das zum Abbestellen verwendet werden kann
     */
    fun subscribeToStream(streamId: Uuid, fromVersion: Long = 0, handler: (DomainEvent) -> Unit): Subscription

    /**
     * Abonniert alle Events von allen Streams.
     *
     * @param fromPosition Die Position, ab der abonniert werden soll (inklusive)
     * @param handler Der Handler, der für jedes Event aufgerufen wird
     * @return Ein Abonnement, das zum Abbestellen verwendet werden kann
     */
    fun subscribeToAll(fromPosition: Long = 0, handler: (DomainEvent) -> Unit): Subscription
}

/**
 * Schnittstelle für ein Abonnement eines Event-Streams.
 */
interface Subscription {
    /**
     * Beendet das Abonnement des Event-Streams.
     */
    fun unsubscribe()

    /**
     * Überprüft, ob das Abonnement aktiv ist.
     *
     * @return true wenn das Abonnement aktiv ist, false andernfalls
     */
    fun isActive(): Boolean
}

/**
 * Exception, die bei einem Nebenläufigkeitskonflikt im Event Store ausgelöst wird.
 */
class ConcurrencyException(message: String) : RuntimeException(message)
