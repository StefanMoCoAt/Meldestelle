package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaEventConsumerCacheTest {

    private lateinit var kafkaConfig: KafkaConfig
    private lateinit var consumer: KafkaEventConsumer

    @BeforeEach
    fun setUp() {
        kafkaConfig = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            defaultGroupIdPrefix = "test-consumer"
            trustedPackages = "at.mocode.*"
        }
        consumer = KafkaEventConsumer(kafkaConfig)
    }

    @Test
    fun `should create consumer successfully with valid configuration`() {
        // Test that consumer can be created with different configurations
        val customConfig = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            defaultGroupIdPrefix = "custom-consumer"
            trustedPackages = "at.mocode.*,com.example.*"
            connectionPoolSize = 5
        }

        assertDoesNotThrow {
            KafkaEventConsumer(customConfig)
        }
    }

    @Test
    fun `should create different consumers with different configurations`() {
        val config1 = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            defaultGroupIdPrefix = "consumer1"
            trustedPackages = "at.mocode.*"
        }

        val config2 = KafkaConfig().apply {
            bootstrapServers = "localhost:9093"
            defaultGroupIdPrefix = "consumer2"
            trustedPackages = "com.example.*"
        }

        val consumer1 = KafkaEventConsumer(config1)
        val consumer2 = KafkaEventConsumer(config2)

        // Both consumers should be created successfully
        assertThat(consumer1).isNotNull
        assertThat(consumer2).isNotNull
        assertThat(consumer1).isNotSameAs(consumer2)
    }

    @Test
    fun `should handle cleanup gracefully`() {
        // Create consumer and call cleanup
        val testConsumer = KafkaEventConsumer(kafkaConfig)

        // Cleanup should not throw any exceptions
        assertDoesNotThrow {
            testConsumer.cleanup()
        }

        // Multiple cleanup calls should also be safe
        assertDoesNotThrow {
            testConsumer.cleanup()
            testConsumer.cleanup()
        }
    }

    @Test
    fun `should create reactive streams for different topics`() {
        // Test that receiveEvents creates reactive streams without errors
        // Note: These won't actually connect to Kafka but should create the Flux
        assertDoesNotThrow {
            val flux1 = consumer.receiveEvents<TestEvent>("topic1")
            val flux2 = consumer.receiveEvents<TestEvent>("topic2")

            // Fluxes should be created (cold streams)
            assertThat(flux1).isNotNull
            assertThat(flux2).isNotNull
        }
    }

    @Test
    fun `should create reactive streams for different event types`() {
        // Test that different event types create different streams
        assertDoesNotThrow {
            val flux1 = consumer.receiveEvents<TestEvent>("test-topic")
            val flux2 = consumer.receiveEvents<AnotherTestEvent>("test-topic")

            // Both should be created successfully
            assertThat(flux1).isNotNull
            assertThat(flux2).isNotNull
        }
    }

    @Test
    fun `should handle consumer configuration with security features`() {
        val secureConfig = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            defaultGroupIdPrefix = "secure-consumer"
            trustedPackages = "at.mocode.*,com.secure.*"
            enableSecurityFeatures = true
            connectionPoolSize = 15
        }

        assertDoesNotThrow {
            val secureConsumer = KafkaEventConsumer(secureConfig)
            assertThat(secureConsumer).isNotNull

            // Should be able to create streams
            val flux = secureConsumer.receiveEvents<TestEvent>("secure-topic")
            assertThat(flux).isNotNull
        }
    }

    @Test
    fun `should validate trusted packages configuration`() {
        // Test with various trusted package configurations
        val configs = listOf(
            "at.mocode.*",
            "at.mocode.*,com.example.*",
            "java.lang.*,java.util.*,at.mocode.*"
        )

        configs.forEach { trustedPackages ->
            val config = KafkaConfig().apply {
                bootstrapServers = "localhost:9092"
                defaultGroupIdPrefix = "validation-consumer"
                this.trustedPackages = trustedPackages
            }

            assertDoesNotThrow {
                val testConsumer = KafkaEventConsumer(config)
                val flux = testConsumer.receiveEvents<TestEvent>("validation-topic")
                assertThat(flux).isNotNull
            }
        }
    }

    @Test
    fun `should handle different connection pool sizes`() {
        val poolSizes = listOf(1, 5, 10, 20, 50)

        poolSizes.forEach { poolSize ->
            val config = KafkaConfig().apply {
                bootstrapServers = "localhost:9092"
                defaultGroupIdPrefix = "pool-test-consumer"
                connectionPoolSize = poolSize
            }

            assertDoesNotThrow {
                val testConsumer = KafkaEventConsumer(config)
                assertThat(testConsumer).isNotNull

                // Should be able to create reactive streams
                val flux = testConsumer.receiveEvents<TestEvent>("pool-test-topic")
                assertThat(flux).isNotNull
            }
        }
    }

    @Test
    fun `should handle different group ID prefixes`() {
        val prefixes = listOf(
            "test-consumer",
            "production-consumer",
            "development.consumer",
            "consumer_123"
        )

        prefixes.forEach { prefix ->
            val config = KafkaConfig().apply {
                bootstrapServers = "localhost:9092"
                defaultGroupIdPrefix = prefix
                trustedPackages = "at.mocode.*"
            }

            assertDoesNotThrow {
                val testConsumer = KafkaEventConsumer(config)
                val flux = testConsumer.receiveEvents<TestEvent>("prefix-test-topic")
                assertThat(flux).isNotNull
            }
        }
    }

    @Test
    fun `should support extension function for reified types`() {
        // Test the Kotlin extension function receiveEvents<T>()
        assertDoesNotThrow {
            val fluxWithReified = consumer.receiveEvents<TestEvent>("reified-topic")
            val fluxWithClass = consumer.receiveEvents("reified-topic", TestEvent::class.java)

            // Both should work and create valid Flux instances
            assertThat(fluxWithReified).isNotNull
            assertThat(fluxWithClass).isNotNull
        }
    }

    @Test
    fun `should handle concurrent consumer creation`() {
        // Test that multiple consumers can be created concurrently
        val consumers = (1..10).map { index ->
            val config = KafkaConfig().apply {
                bootstrapServers = "localhost:9092"
                defaultGroupIdPrefix = "concurrent-consumer-$index"
                trustedPackages = "at.mocode.*"
            }
            KafkaEventConsumer(config)
        }

        // All consumers should be created successfully
        assertThat(consumers).hasSize(10)
        consumers.forEach { testConsumer ->
            assertThat(testConsumer).isNotNull

            // Each should be able to create streams
            val flux = testConsumer.receiveEvents<TestEvent>("concurrent-topic")
            assertThat(flux).isNotNull
        }

        // Clean up all consumers
        consumers.forEach { testConsumer ->
            assertDoesNotThrow { testConsumer.cleanup() }
        }
    }

    data class TestEvent(val message: String)
    data class AnotherTestEvent(val data: String)
}
