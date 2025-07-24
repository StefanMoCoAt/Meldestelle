package at.mocode.infrastructure.messaging.client

import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Kafka implementation of EventPublisher.
 */
@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    override suspend fun publishEvent(topic: String, key: String?, event: Any) {
        try {
            logger.debug("Publishing event to topic '{}' with key '{}'", topic, key)

            val sendResult = if (key != null) {
                kafkaTemplate.send(topic, key, event).get()
            } else {
                kafkaTemplate.send(topic, event).get()
            }

            logger.info("Successfully published event to topic '{}' with key '{}'", topic, key)
        } catch (exception: Exception) {
            logger.error("Failed to publish event to topic '{}' with key '{}'", topic, key, exception)
            throw exception
        }
    }

    override suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>) {
        try {
            logger.debug("Publishing {} events to topic '{}'", events.size, topic)

            events.forEach { (key, event) ->
                publishEvent(topic, key, event)
            }

            logger.info("Successfully published {} events to topic '{}'", events.size, topic)
        } catch (exception: Exception) {
            logger.error("Failed to publish events to topic '{}'", topic, exception)
            throw exception
        }
    }
}
