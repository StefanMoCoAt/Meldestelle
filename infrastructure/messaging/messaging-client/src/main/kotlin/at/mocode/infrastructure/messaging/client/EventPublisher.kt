package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Schnittstelle zum Publizieren von Domain-Events in den Message-Broker.
 *
 * Folgt DDD-Prinzipien mit expliziter Fehlerbehandlung über domänenspezifische Fehlertypen.
 * Alle Operationen verwenden das Result-Pattern für typsichere Fehlerbehandlung.
 */
interface EventPublisher {

    /**
     * Veröffentlicht ein einzelnes Event in das angegebene Topic.
     *
     * @param topic Das Kafka-Topic
     * @param key Optionaler Schlüssel für Partitionierung
     * @param event Das zu veröffentlichende Domain-Event
     * @return Result<Unit> bei Erfolg oder MessagingError bei spezifischem Fehler
     */
    suspend fun publishEvent(topic: String, key: String? = null, event: Any): Result<Unit>

    /**
     * Veröffentlicht mehrere Events als Batch in das angegebene Topic.
     *
     * @param topic Das Kafka-Topic
     * @param events Liste aus (Key, Event)-Paaren
     * @return Result<List<Unit>> bei Erfolg oder MessagingError bei Fehlern
     */
    suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Result<List<Unit>>

    /**
     * Legacy reaktive Methoden für Abwärtskompatibilität.
     * Diese werden zugunsten der Result-basierten Methoden mittelfristig entfernt.
     */
    @Deprecated("Use suspending publishEvent with Result instead", ReplaceWith("publishEvent(topic, key, event)"))
    fun publishEventReactive(topic: String, key: String? = null, event: Any): Mono<Unit>

    @Deprecated("Use suspending publishEvents with Result instead", ReplaceWith("publishEvents(topic, events)"))
    fun publishEventsReactive(topic: String, events: List<Pair<String?, Any>>): Flux<Unit>
}
