package at.mocode.infrastructure.messaging.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.reactive.asPublisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

/**
 * Generische Schnittstelle zum Konsumieren von Events aus einem Message-Broker.
 *
 * Folgt DDD-Prinzipien mit expliziter Fehlerbehandlung über domänenspezifische Fehlertypen.
 * Bietet sowohl Result-basierte Methoden als auch reaktive Streams für Flexibilität.
 */
interface EventConsumer {

    /**
     * Empfängt Events vom angegebenen Topic mit expliziter Fehlerbehandlung.
     *
     * @param T Erwarteter Typ der Event-Payload
     * @param topic Das zu abonnierende Topic
     * @param eventType Der Klassen-Typ der zu konsumierenden Events
     * @return Flow<Result<T>> wobei jedes Result entweder ein erfolgreiches Event oder einen MessagingError enthält
     */
    fun <T : Any> receiveEventsWithResult(topic: String, eventType: Class<T>): Flow<Result<T>>

    /**
     * Legacy reaktive Methode zum Empfangen von Events.
     *
     * Diese Methode liefert einen "kalten" Flux, d. h. der Consumer beginnt erst
     * nach Subscription mit dem Empfang von Nachrichten.
     *
     * @param T Erwarteter Typ der Event-Payload.
     * @param topic Das zu abonnierende Topic.
     * @return Ein reaktiver Stream (Flux) von Events des Typs T.
     */
    @Deprecated("Use receiveEventsWithResult with Flow<Result<T>> instead", ReplaceWith("receiveEventsWithResult(topic, eventType)"))
    fun <T : Any> receiveEvents(topic: String, eventType: Class<T>): Flux<T>
}

/**
 * Kotlin-idiomatische Extension-Funktion für `receiveEventsWithResult` mit reified Typen.
 *
 * Beispiel: `consumer.receiveEventsWithResult<MyEvent>("my-topic").collect { result -> ... }`
 */
inline fun <reified T : Any> EventConsumer.receiveEventsWithResult(topic: String): Flow<Result<T>> {
    return this.receiveEventsWithResult(topic, T::class.java)
}

/**
 * Kotlin-idiomatische Extension-Funktion für `receiveEvents` mit reified Typen.
 *
 * Beispiel: `consumer.receiveEvents<MyEvent>("my-topic").subscribe { ... }`
 */
@Deprecated("Use receiveEventsWithResult with Flow<Result<T>> instead", ReplaceWith("receiveEventsWithResult<T>(topic)"))
inline fun <reified T : Any> EventConsumer.receiveEvents(topic: String): Flux<T> {
    // Convert Flow<Result<T>> to Flux<T> for backward compatibility
    // New behavior: emit only successful events; log failures instead of throwing to keep the stream alive
    val logger = LoggerFactory.getLogger("EventConsumerExtensions")
    return this.receiveEventsWithResult<T>(topic)
        .mapNotNull { result: Result<T> ->
            result.getOrElse {
                logger.warn("Dropping failed event in legacy receiveEvents: {}", it.message)
                null
            }
        }
        .asPublisher()
        .let { Flux.from(it) }
}
