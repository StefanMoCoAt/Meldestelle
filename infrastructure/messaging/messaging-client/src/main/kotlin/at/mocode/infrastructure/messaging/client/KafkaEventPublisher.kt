package at.mocode.infrastructure.messaging.client

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * A reactive, non-blocking Kafka implementation of EventPublisher.
 */
@Component
class KafkaEventPublisher(
    // KORREKTUR: Verwendung des reaktiven Templates
    private val reactiveKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    override fun publishEvent(topic: String, key: String?, event: Any): Mono<Void> {
        logger.debug("Publishing event to topic '{}' with key '{}'", topic, key)
        return reactiveKafkaTemplate.send(topic, key, event)
            .doOnSuccess { result ->
                val record = result.recordMetadata()
                logger.info(
                    "Successfully published event to topic-partition {}-{} with offset {}",
                    record.topic(), record.partition(), record.offset()
                )
            }
            .doOnError { exception ->
                logger.error("Failed to publish event to topic '{}' with key '{}'", topic, key, exception)
            }
            .then() // Wandelt das Ergebnis in ein Mono<Void> um
    }

    override fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Flux<Void> {
        logger.debug("Publishing {} events to topic '{}'", events.size, topic)
        // Verwendet Flux.fromIterable, um eine Sequenz von Sende-Operationen zu erstellen
        return Flux.fromIterable(events)
            // .flatMap stellt sicher, dass die Sende-Operationen parallelisiert,
            // aber dennoch reaktiv (nicht-blockierend) ausgeführt werden.
            .flatMap { (key, event) ->
                publishEvent(topic, key, event)
            }
    }
}
