package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.util.retry.Retry
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A reactive, non-blocking Kafka implementation of the EventConsumer interface
 * with optimized connection pooling, security, and error handling.
 */
@Component
class KafkaEventConsumer(
    private val kafkaConfig: KafkaConfig
) : EventConsumer {

    private val logger = LoggerFactory.getLogger(KafkaEventConsumer::class.java)

    // Connection pool to reuse KafkaReceiver instances per topic-eventType combination
    private val receiverCache = ConcurrentHashMap<String, KafkaReceiver<String, Any>>()

    override fun <T : Any> receiveEventsWithResult(topic: String, eventType: Class<T>): Flow<Result<T>> {
        logger.info("Setting up Result-based consumer for topic '{}' with event type '{}'", topic, eventType.simpleName)

        return receiveEvents(topic, eventType)
            .map<Result<T>> { event -> Result.success(event) }
            .onErrorContinue { error, _ ->
                logger.warn("Error occurred while consuming events from topic '{}' for event type '{}': {}",
                    topic, eventType.simpleName, error.message)
            }
            .doOnError { exception ->
                logger.error("Fatal error in consumer stream for topic '{}' and event type '{}': {}",
                    topic, eventType.simpleName, exception.message, exception)
            }
            .asFlow()
    }

    override fun <T : Any> receiveEvents(topic: String, eventType: Class<T>): Flux<T> {
        logger.info("Setting up reactive consumer for topic '{}' with event type '{}'", topic, eventType.simpleName)

        val cacheKey = "${topic}-${eventType.name}"
        val groupId = "${kafkaConfig.defaultGroupIdPrefix}-${topic}-${eventType.simpleName.lowercase()}"

        // Get or create a cached receiver for this topic-eventType combination
        @Suppress("UNCHECKED_CAST")
        val receiver = receiverCache.computeIfAbsent(cacheKey) {
            createOptimizedReceiver<T>(topic, eventType) as KafkaReceiver<String, Any>
        } as KafkaReceiver<String, T>

        return receiver.receive()
            .doOnNext { record ->
                logger.debug(
                    "Received message from topic-partition {}-{} with offset {} for event type '{}' [groupId={}, timestamp={}]",
                    record.topic(), record.partition(), record.offset(), eventType.simpleName,
                    groupId, record.timestamp()
                )
            }
            .map { record ->
                // Manual commit acknowledgment for better control
                record.receiverOffset().acknowledge()
                record.value()
            }
            .doOnError { exception ->
                logger.error("Error receiving events from topic '{}' for event type '{}' [groupId={}, cacheKey={}]: {}",
                    topic, eventType.simpleName, groupId, cacheKey, exception.message, exception)
            }
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(10))
                    .doBeforeRetry { retrySignal ->
                        logger.warn("Retrying consumer for topic '{}', attempt: {}, error: {}",
                            topic, retrySignal.totalRetries() + 1, retrySignal.failure().message)
                    }
                    .onRetryExhaustedThrow { _, retrySignal ->
                        logger.error("Consumer retry exhausted for topic '{}' after {} attempts",
                            topic, retrySignal.totalRetries())
                        retrySignal.failure()
                    }
            )
    }

    /**
     * Creates an optimized KafkaReceiver with secure configuration and performance tuning.
     */
    private fun <T : Any> createOptimizedReceiver(topic: String, eventType: Class<T>): KafkaReceiver<String, T> {
        // Generate unique group ID for this consumer instance
        val groupId = "${kafkaConfig.defaultGroupIdPrefix}-${topic}-${eventType.simpleName.lowercase()}"
        val consumerConfig = kafkaConfig.consumerConfigs(groupId)

        // Create type-safe JSON deserializer with restricted trusted packages
        val jsonDeserializer = JsonDeserializer(eventType).apply {
            // Use restricted trusted packages instead of wildcard for security
            addTrustedPackages(kafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }

        val receiverOptions = ReceiverOptions.create<String, T>(consumerConfig)
            .subscription(Collections.singleton(topic))
            .withValueDeserializer(jsonDeserializer)
            .addAssignListener { partitions ->
                logger.info("Consumer '{}' assigned partitions for topic '{}': {}",
                    groupId, topic, partitions.map { "${it.topicPartition().topic()}-${it.topicPartition().partition()}" })
            }
            .addRevokeListener { partitions ->
                logger.warn("Consumer '{}' revoked partitions for topic '{}': {}",
                    groupId, topic, partitions.map { "${it.topicPartition().topic()}-${it.topicPartition().partition()}" })
            }
            // Enable commit interval for manual acknowledgment control
            .commitInterval(Duration.ofSeconds(5))
            .commitBatchSize(100)

        return KafkaReceiver.create(receiverOptions)
    }

    /**
     * Cleanup method to clear cached receivers on application shutdown.
     * Reactive receivers will be automatically cleaned up when their streams complete.
     */
    @jakarta.annotation.PreDestroy
    fun cleanup() {
        logger.info("Cleaning up Kafka consumer cache...")
        val cacheSize = receiverCache.size
        receiverCache.clear()
        logger.info("Kafka consumer cleanup completed. Cleared {} cached receivers", cacheSize)
    }
}
