package at.mocode.infrastructure.messaging.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

/**
 * Central Kafka producer configuration used across modules.
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
     * Common producer properties with sensible defaults (String keys, JSON values).
     */
    fun producerConfigs(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        // Avoid adding type info headers; keeps payloads simple and interoperable.
        JsonSerializer.ADD_TYPE_INFO_HEADERS to false
    )

    /**
     * Strongly typed producer factory to avoid unchecked casts in consumers/tests.
     */
    fun producerFactory(): DefaultKafkaProducerFactory<String, Any> =
        DefaultKafkaProducerFactory(producerConfigs())
}
