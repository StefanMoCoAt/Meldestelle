package at.mocode.infrastructure.messaging.client

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.util.Collections

/**
 * A reactive, non-blocking Kafka implementation of the EventConsumer interface.
 */
@Component
class KafkaEventConsumer(
    // Wir injizieren die Basis-Konfigurationseigenschaften aus messaging-config
    private val consumerConfig: Map<String, Any>
) : EventConsumer {

    private val logger = LoggerFactory.getLogger(KafkaEventConsumer::class.java)

    override fun <T : Any> receiveEvents(topic: String, eventType: Class<T>): Flux<T> {
        // Für jeden Aufruf wird eine neue, spezifische Konfiguration für diesen Topic erstellt.
        val receiverOptions = ReceiverOptions.create<String, T>(consumerConfig)
            .subscription(Collections.singleton(topic))
            .withValueDeserializer(JsonDeserializer(eventType).trustedPackages("*"))
            .addAssignListener { partitions ->
                logger.info("Partitions assigned for topic '{}': {}", topic, partitions)
            }
            .addRevokeListener { partitions ->
                logger.warn("Partitions revoked for topic '{}': {}", topic, partitions)
            }

        return KafkaReceiver.create(receiverOptions)
            .receive()
            .doOnNext { record ->
                logger.debug(
                    "Received message from topic-partition {}-{} with offset {}",
                    record.topic(), record.partition(), record.offset()
                )
            }
            .map { it.value() } // Extrahiere nur die deserialisierte Nachricht
            .doOnError { exception ->
                logger.error("Error receiving events from topic '{}'", topic, exception)
            }
    }
}
