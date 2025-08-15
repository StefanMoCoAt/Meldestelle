package at.mocode.infrastructure.messaging.client

import kotlinx.coroutines.reactor.awaitSingle
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
 *
 * Implements both Result-based methods (preferred) and reactive methods (legacy).
 * Follows DDD principles with explicit error handling using domain-specific error types.
 */
@Component
class KafkaEventPublisher(
    private val reactiveKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    companion object {
        /** Maximum number of retry attempts for failed message publishing operations */
        private const val MAX_RETRY_ATTEMPTS = 3L

        /** Initial delay in seconds between retry attempts */
        private const val RETRY_DELAY_SECONDS = 1L

        /** Maximum backoff delay in seconds for exponential backoff retry strategy */
        private const val MAX_BACKOFF_SECONDS = 10L

        /** Default concurrency level for batch processing operations */
        private const val BATCH_CONCURRENCY_LEVEL = 10

        /** Progress logging interval for batch operations (every N events) */
        private const val BATCH_PROGRESS_LOG_INTERVAL = 100
    }

    override suspend fun publishEvent(topic: String, key: String?, event: Any): Result<Unit> {
        return try {
            publishEventReactive(topic, key, event).awaitSingle()
            Result.success(Unit)
        } catch (exception: Throwable) {
            Result.failure(mapToMessagingError(exception))
        }
    }

    override suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Result<List<Unit>> {
        return try {
            val results = publishEventsReactive(topic, events).collectList().awaitSingle()
            Result.success(results)
        } catch (exception: Throwable) {
            Result.failure(mapToMessagingError(exception))
        }
    }

    override fun publishEventReactive(topic: String, key: String?, event: Any): Mono<Unit> {
        logger.debug("Publishing event to topic '{}' with key '{}', event type: '{}'",
            topic, key, event::class.simpleName)

        return reactiveKafkaTemplate.send(topic, key ?: "", event)
            .doOnSuccess { result ->
                val record = result.recordMetadata()
                logger.debug(
                    "Successfully published event to topic-partition {}-{} with offset {} (key: '{}')",
                    record.topic(), record.partition(), record.offset(), key
                )
            }
            .doOnError { exception ->
                logger.warn("Failed to publish event to topic '{}' with key '{}' [eventType={}, retryable={}] - will retry if configured: {}",
                    topic, key, event::class.simpleName, isRetryableException(exception), exception.message, exception)
            }
            .retryWhen(createRetrySpec(topic, key))
            .doOnError { exception ->
                logger.error("Final failure after retries: Failed to publish event to topic '{}' with key '{}'",
                    topic, key, exception)
            }
            .map { Unit }
    }

    override fun publishEventsReactive(topic: String, events: List<Pair<String?, Any>>): Flux<Unit> {
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
                reactiveKafkaTemplate.send(topic, key ?: "", event)
                    .doOnSuccess { result ->
                        val record = result.recordMetadata()
                        logger.debug("Successfully published event to topic-partition {}-{} with offset {} (key: '{}')",
                            record.topic(), record.partition(), record.offset(), key)
                        if ((index + 1) % BATCH_PROGRESS_LOG_INTERVAL == 0L || index == events.size.toLong() - 1) {
                            logger.info("Batch progress: {}/{} events published to topic '{}'",
                                index + 1, events.size, topic)
                        }
                    }
                    .doOnError { exception ->
                        logger.warn("Failed to publish event {} in batch to topic '{}' with key '{}' [eventType={}, retryable={}] - will retry if configured: {}",
                            index + 1, topic, key, event::class.simpleName, isRetryableException(exception), exception.message, exception)
                    }
                    .retryWhen(createRetrySpec(topic, key))
                    .map { Unit } // Convert to Mono<Unit> that emits one Unit per successful send
                    .onErrorContinue { error, _ ->
                        logger.error("Error publishing event {} in batch to topic '{}': {}",
                            index + 1, topic, error.message)
                    }
            }, BATCH_CONCURRENCY_LEVEL) // Controlled concurrency for better resource management
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
        Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(RETRY_DELAY_SECONDS))
            .maxBackoff(Duration.ofSeconds(MAX_BACKOFF_SECONDS))
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
     * Maps generic exceptions to domain-specific MessagingError types.
     */
    private fun mapToMessagingError(exception: Throwable): MessagingError {
        return when {
            exception.message?.contains("serializ", ignoreCase = true) == true ->
                MessagingError.SerializationError("Serialization failed: ${exception.message}", exception)
            exception.message?.contains("timeout", ignoreCase = true) == true ||
            exception is java.util.concurrent.TimeoutException ->
                MessagingError.TimeoutError("Operation timed out: ${exception.message}", exception)
            exception.message?.contains("connection", ignoreCase = true) == true ||
            exception.message?.contains("network", ignoreCase = true) == true ||
            exception is java.net.ConnectException ||
            exception is java.io.IOException ->
                MessagingError.ConnectionError("Connection failed: ${exception.message}", exception)
            exception.message?.contains("auth", ignoreCase = true) == true ->
                MessagingError.AuthenticationError("Authentication failed: ${exception.message}", exception)
            exception.message?.contains("topic", ignoreCase = true) == true ->
                MessagingError.TopicConfigurationError("Topic configuration error: ${exception.message}", exception)
            else -> MessagingError.UnexpectedError("Unexpected error: ${exception.message}", exception)
        }
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
