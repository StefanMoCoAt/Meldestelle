package at.mocode.infrastructure.messaging.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

/**
 * Central Kafka configuration used across modules with optimized settings for performance and reliability.
 *
 * This class can be instantiated programmatically (as done in tests) or
 * registered as a Spring @Configuration with @Bean methods in an application context.
 */
class KafkaConfig {

    /**
     * Comma-separated list of host:port pairs used for establishing the initial connection to the Kafka cluster.
     */
    var bootstrapServers: String = "localhost:9092"

    /**
     * Default consumer group ID prefix.
     */
    var defaultGroupIdPrefix: String = "messaging-client"

    /**
     * Comma-separated list of trusted packages for JSON deserialization security.
     * Default restricts to application packages only.
     */
    var trustedPackages: String = "at.mocode.*"

    /**
     * Optimized producer properties with performance tuning and reliability settings.
     */
    fun producerConfigs(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        // Avoid adding type info headers; keeps payloads simple and interoperable.
        JsonSerializer.ADD_TYPE_INFO_HEADERS to false,

        // Performance optimizations
        ProducerConfig.BATCH_SIZE_CONFIG to 32768, // 32KB batch size for better throughput
        ProducerConfig.LINGER_MS_CONFIG to 5, // Wait up to 5ms to batch messages
        ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy", // Fast compression
        ProducerConfig.BUFFER_MEMORY_CONFIG to 67108864, // 64MB buffer memory

        // Reliability settings
        ProducerConfig.ACKS_CONFIG to "all", // Wait for all replicas
        ProducerConfig.RETRIES_CONFIG to 3, // Retry failed sends
        ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 1000, // 1 second retry backoff
        ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 30000, // 30 second delivery timeout
        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 10000, // 10 second request timeout

        // Idempotence for exactly-once semantics
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5
    )

    /**
     * Optimized consumer properties with performance tuning and reliability settings.
     */
    fun consumerConfigs(groupId: String? = null): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to (groupId ?: "${defaultGroupIdPrefix}-${System.currentTimeMillis()}"),
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,

        // JSON deserialization security
        JsonDeserializer.TRUSTED_PACKAGES to trustedPackages,
        JsonDeserializer.USE_TYPE_INFO_HEADERS to false,

        // Performance optimizations
        ConsumerConfig.FETCH_MIN_BYTES_CONFIG to 1024, // 1KB minimum fetch size
        ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 500, // Max 500ms wait for fetch
        ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG to 1048576, // 1MB max partition fetch
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500, // Process up to 500 records per poll

        // Reliability settings
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false, // Manual commit for better control
        ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000, // 30 second session timeout
        ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 3000, // 3 second heartbeat

        // Connection settings
        ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG to 540000, // 9 minutes idle timeout
        ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG to 50,
        ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG to 1000
    )

    /**
     * Strongly typed producer factory to avoid unchecked casts in consumers/tests.
     */
    fun producerFactory(): DefaultKafkaProducerFactory<String, Any> =
        DefaultKafkaProducerFactory(producerConfigs())
}
