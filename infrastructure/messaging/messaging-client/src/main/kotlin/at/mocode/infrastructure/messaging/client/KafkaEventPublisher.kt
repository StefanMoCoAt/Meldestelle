package at.mocode.infrastructure.messaging.client

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

/**
 * A reactive, non-blocking Kafka implementation of EventPublisher with enhanced
 * error handling, retry mechanisms, and optimized batch processing.
 */
@Component
class KafkaEventPublisher(
    private val reactiveKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    companion object {
        private const val DEFAULT_RETRY_ATTEMPTS = 3L
        private const val DEFAULT_RETRY_DELAY_SECONDS = 1L
        private const val DEFAULT_MAX_BACKOFF_SECONDS = 10L
        private const val DEFAULT_BATCH_CONCURRENCY = 10
    }

    override fun publishEvent(topic: String, key: String?, event: Any): Mono<Void> {
        logger.debug("Publishing event to topic '{}' with key '{}', event type: '{}'",
            topic, key, event::class.simpleName)

        return reactiveKafkaTemplate.send(topic, key, event)
            .doOnSuccess { result ->
                val record = result.recordMetadata()
                logger.debug(
                    "Successfully published event to topic-partition {}-{} with offset {} (key: '{}')",
                    record.topic(), record.partition(), record.offset(), key
                )
            }
            .doOnError { exception ->
                logger.warn("Failed to publish event to topic '{}' with key '{}' - will retry if configured",
                    topic, key, exception)
            }
            .retryWhen(createRetrySpec(topic, key))
            .doOnError { exception ->
                logger.error("Final failure after retries: Failed to publish event to topic '{}' with key '{}'",
                    topic, key, exception)
            }
            .then()
    }

    override fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Flux<Void> {
        if (events.isEmpty()) {
            logger.debug("No events to publish to topic '{}'", topic)
            return Flux.empty()
        }

        logger.info("Publishing {} events to topic '{}' using optimized batch processing", events.size, topic)

        return Flux.fromIterable(events)
            .index() // Add index for progress tracking
            .flatMap({ indexedEventPair ->
                val index = indexedEventPair.t1
                val eventPair = indexedEventPair.t2
                val (key, event) = eventPair
                publishEvent(topic, key, event)
                    .doOnSuccess {
                        if ((index + 1) % 100 == 0L || index == events.size.toLong() - 1) {
                            logger.info("Batch progress: {}/{} events published to topic '{}'",
                                index + 1, events.size, topic)
                        }
                    }
                    .onErrorContinue { error, _ ->
                        logger.error("Error publishing event {} in batch to topic '{}': {}",
                            index + 1, topic, error.message)
                    }
            }, DEFAULT_BATCH_CONCURRENCY) // Controlled concurrency for better resource management
            .doOnComplete {
                logger.info("Completed publishing batch of {} events to topic '{}'", events.size, topic)
            }
            .doOnError { error ->
                logger.error("Batch publishing to topic '{}' failed with error: {}", topic, error.message)
            }
    }

    /**
     * Creates a retry specification with exponential backoff for robust error handling.
     */
    private fun createRetrySpec(topic: String, key: String?): Retry =
        Retry.backoff(DEFAULT_RETRY_ATTEMPTS, Duration.ofSeconds(DEFAULT_RETRY_DELAY_SECONDS))
            .maxBackoff(Duration.ofSeconds(DEFAULT_MAX_BACKOFF_SECONDS))
            .filter { exception ->
                // Only retry on transient errors (not serialization errors, etc.)
                isRetryableException(exception)
            }
            .doBeforeRetry { retrySignal ->
                logger.info("Retrying publish to topic '{}' with key '{}', attempt: {}, error: {}",
                    topic, key, retrySignal.totalRetries() + 1,
                    retrySignal.failure().message?.take(100))
            }
            .onRetryExhaustedThrow { _, retrySignal ->
                logger.error("Retry exhausted for topic '{}' with key '{}' after {} attempts",
                    topic, key, retrySignal.totalRetries())
                retrySignal.failure()
            }

    /**
     * Determines if an exception is retryable based on its type and characteristics.
     */
    private fun isRetryableException(exception: Throwable): Boolean {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true -> true
            exception.message?.contains("connection", ignoreCase = true) == true -> true
            exception.message?.contains("network", ignoreCase = true) == true -> true
            exception is java.util.concurrent.TimeoutException -> true
            exception is java.net.ConnectException -> true
            exception is java.io.IOException -> true
            // Don't retry serialization errors or authentication failures
            exception.message?.contains("serializ", ignoreCase = true) == true -> false
            exception.message?.contains("auth", ignoreCase = true) == true -> false
            else -> true // Default to retryable for unknown exceptions
        }
    }
}
