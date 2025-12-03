package at.mocode.infrastructure.eventstore.redis

import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

/**
 * Comprehensive test suite for RedisEventStoreConfiguration.
 *
 * Tests all aspects of Spring Boot autoconfiguration including:
 * - Configuration properties binding
 * - Bean creation and dependency injection
 * - Default value handling
 * - Property conversion and validation
 * - Conditional bean creation
 */
@DisplayName("RedisEventStoreConfiguration Tests")
class RedisEventStoreConfigurationTest {

    private val logger = LoggerFactory.getLogger(RedisEventStoreConfigurationTest::class.java)

    @Configuration
    @EnableConfigurationProperties(RedisEventStoreProperties::class)
    class TestConfiguration

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration::class.java,
            org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration::class.java,
            RedisEventStoreConfiguration::class.java
        ))
        .withUserConfiguration(TestConfiguration::class.java)

    @Test
    @DisplayName("Should create all beans with custom configuration properties")
    fun `should create beans with custom configuration properties`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.host=custom-redis-host",
                "redis.event-store.port=6380",
                "redis.event-store.consumer-group=custom-group",
                "redis.event-store.max-batch-size=50"
            )
            .run { context ->
                // Verify properties are correctly bound
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)
                assertEquals("custom-redis-host", properties.host)
                assertEquals(6380, properties.port)
                assertEquals("custom-group", properties.consumerGroup)
                assertEquals(50, properties.maxBatchSize)

                // Verify all beans are created
                assertTrue(context.containsBean("eventStoreRedisConnectionFactory"))
                assertTrue(context.containsBean("eventStoreRedisTemplate"))
                assertTrue(context.containsBean("eventSerializer"))
                assertTrue(context.containsBean("eventStore"))
                assertTrue(context.containsBean("eventConsumer"))

                // Verify bean types
                assertNotNull(context.getBean("eventStoreRedisConnectionFactory", RedisConnectionFactory::class.java))
                assertNotNull(context.getBean("eventStoreRedisTemplate", StringRedisTemplate::class.java))
                assertNotNull(context.getBean("eventSerializer", EventSerializer::class.java))
                assertNotNull(context.getBean("eventStore", EventStore::class.java))
                assertNotNull(context.getBean("eventConsumer", RedisEventConsumer::class.java))

                logger.debug("Custom configuration test passed - all beans created with custom properties")
            }
    }

    @Test
    @DisplayName("Should fallback to default configuration when properties are missing")
    fun `should fallback to default configuration when properties missing`() {
        contextRunner
            .run { context ->
                // Verify properties use defaults
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)
                assertEquals("localhost", properties.host)
                assertEquals(6379, properties.port)
                assertNull(properties.password)
                assertEquals(0, properties.database)
                assertEquals(2000L, properties.connectionTimeout)
                assertEquals(2000L, properties.readTimeout)
                assertTrue(properties.usePooling)
                assertEquals(8, properties.maxPoolSize)
                assertEquals(2, properties.minPoolSize)
                assertEquals("event-processors", properties.consumerGroup)
                assertEquals("event-consumer", properties.consumerName)
                assertEquals("event-stream:", properties.streamPrefix)
                assertEquals("all-events", properties.allEventsStream)
                assertEquals(Duration.ofMinutes(1), properties.claimIdleTimeout)
                assertEquals(Duration.ofMillis(100), properties.pollTimeout)
                assertEquals(100, properties.maxBatchSize)
                assertTrue(properties.createConsumerGroupIfNotExists)

                // Verify all required beans are still created by defaults
                assertTrue(context.containsBean("eventStoreRedisConnectionFactory"))
                assertTrue(context.containsBean("eventStoreRedisTemplate"))
                assertTrue(context.containsBean("eventSerializer"))
                assertTrue(context.containsBean("eventStore"))
                assertTrue(context.containsBean("eventConsumer"))

                logger.debug("Default configuration test passed - all beans created with default values")
            }
    }

    @Test
    @DisplayName("Should handle partial configuration correctly with mixed custom and default properties")
    fun `should handle partial configuration correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.host=partial-host",
                "redis.event-store.consumer-group=partial-group"
                // Other properties should use defaults
            )
            .run { context ->
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify custom properties are set
                assertEquals("partial-host", properties.host)
                assertEquals("partial-group", properties.consumerGroup)

                // Verify defaults are used for unspecified properties
                assertEquals(6379, properties.port) // Default
                assertEquals("event-consumer", properties.consumerName) // Default
                assertEquals("event-stream:", properties.streamPrefix) // Default

                // All beans should still be created
                assertTrue(context.containsBean("eventStoreRedisConnectionFactory"))
                assertTrue(context.containsBean("eventStore"))
                assertTrue(context.containsBean("eventConsumer"))

                logger.debug("Partial configuration test passed - mixed custom/default properties work")
            }
    }

    @Test
    @DisplayName("Should handle Redis connection factory creation correctly")
    fun `should handle Redis connection factory creation correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.host=test-host",
                "redis.event-store.port=6380",
                "redis.event-store.password=test-password",
                "redis.event-store.database=1"
            )
            .run { context ->
                val connectionFactory = context.getBean("eventStoreRedisConnectionFactory", RedisConnectionFactory::class.java)
                assertNotNull(connectionFactory)

                // Verify the connection factory is properly configured
                // Note: We can't easily test the internal configuration without making actual connections,
                // but we can verify the bean is created and is the right type
                assertTrue(connectionFactory::class.java.name.contains("LettuceConnectionFactory"))

                logger.debug("Redis connection factory creation test passed")
            }
    }

    @Test
    fun `should handle Redis template creation correctly`() {
        contextRunner
            .run { context ->
                val redisTemplate = context.getBean("eventStoreRedisTemplate", StringRedisTemplate::class.java)
                assertNotNull(redisTemplate)

                // Verify the template is properly set up
                assertNotNull(redisTemplate.connectionFactory)

                logger.debug("Redis template creation test passed")
            }
    }

    @Test
    fun `should create EventSerializer with correct type`() {
        contextRunner
            .run { context ->
                val eventSerializer = context.getBean("eventSerializer", EventSerializer::class.java)
                assertNotNull(eventSerializer)

                // Verify it's the Jackson implementation
                assertTrue(eventSerializer is JacksonEventSerializer)

                logger.debug("EventSerializer creation test passed - JacksonEventSerializer created")
            }
    }

    @Test
    fun `should create EventStore with correct dependencies`() {
        contextRunner
            .run { context ->
                val eventStore = context.getBean("eventStore", EventStore::class.java)
                assertNotNull(eventStore)

                // Verify it's the Redis implementation
                assertTrue(eventStore is RedisEventStore)

                // Verify dependencies are wired correctly
                val redisTemplate = context.getBean("eventStoreRedisTemplate", StringRedisTemplate::class.java)
                val eventSerializer = context.getBean("eventSerializer", EventSerializer::class.java)
                val properties = context.getBean(RedisEventStoreProperties::class.java)

                assertNotNull(redisTemplate)
                assertNotNull(eventSerializer)
                assertNotNull(properties)

                logger.debug("EventStore creation test passed - RedisEventStore created with dependencies")
            }
    }

    @Test
    fun `should create EventConsumer with correct dependencies`() {
        contextRunner
            .run { context ->
                val eventConsumer = context.getBean("eventConsumer", RedisEventConsumer::class.java)
                assertNotNull(eventConsumer)

                // Verify dependencies are available
                val redisTemplate = context.getBean("eventStoreRedisTemplate", StringRedisTemplate::class.java)
                val eventSerializer = context.getBean("eventSerializer", EventSerializer::class.java)
                val properties = context.getBean(RedisEventStoreProperties::class.java)

                assertNotNull(redisTemplate)
                assertNotNull(eventSerializer)
                assertNotNull(properties)

                logger.debug("EventConsumer creation test passed - RedisEventConsumer created with dependencies")
            }
    }

    @Test
    fun `should handle boolean and numeric property conversion correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.use-pooling=false",
                "redis.event-store.max-pool-size=16",
                "redis.event-store.min-pool-size=4",
                "redis.event-store.max-batch-size=25",
                "redis.event-store.create-consumer-group-if-not-exists=false"
            )
            .run { context ->
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify boolean properties
                assertFalse(properties.usePooling)
                assertFalse(properties.createConsumerGroupIfNotExists)

                // Verify numeric properties
                assertEquals(16, properties.maxPoolSize)
                assertEquals(4, properties.minPoolSize)
                assertEquals(25, properties.maxBatchSize)

                logger.debug("Property type conversion test passed - boolean and numeric values handled correctly")
            }
    }

    @Test
    fun `should handle Duration property conversion correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.claim-idle-timeout=5m",        // 5 minutes
                "redis.event-store.poll-timeout=500ms"            // 500 milliseconds
            )
            .run { context ->
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify Duration properties
                assertEquals(Duration.ofMinutes(5), properties.claimIdleTimeout)
                assertEquals(Duration.ofMillis(500), properties.pollTimeout)

                logger.debug("Duration property conversion test passed")
            }
    }

    @Test
    fun `should handle ConditionalOnMissingBean annotations correctly`() {
        contextRunner
            .withBean("eventSerializer", EventSerializer::class.java, { JacksonEventSerializer() })
            .run { context ->
                // Should use the manually provided bean instead of creating a new one
                val eventSerializer = context.getBean("eventSerializer", EventSerializer::class.java)
                assertNotNull(eventSerializer)

                // Should still create other beans
                assertTrue(context.containsBean("eventStore"))
                assertTrue(context.containsBean("eventConsumer"))

                logger.debug("ConditionalOnMissingBean test passed - manual bean used, others created")
            }
    }

    @Test
    @DisplayName("Should handle boundary property values correctly")
    fun `should handle boundary property values correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.port=65535",  // Maximum valid port
                "redis.event-store.max-batch-size=1",  // Minimum valid batch size
                "redis.event-store.connection-timeout=1",  // Minimum valid timeout
                "redis.event-store.database=15"  // High database number
            )
            .run { context ->
                // Context should start with boundary values
                assertTrue(context.isRunning)
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify boundary values are accepted
                assertEquals(65535, properties.port)
                assertEquals(1, properties.maxBatchSize)
                assertEquals(1L, properties.connectionTimeout)
                assertEquals(15, properties.database)

                logger.debug("[DEBUG_LOG] Boundary property values test passed")
            }
    }

    @Test
    @DisplayName("Should handle complex Duration configurations correctly")
    fun `should handle complex Duration configurations correctly`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.claim-idle-timeout=PT30S",        // 30 seconds
                "redis.event-store.poll-timeout=PT1.5S"              // 1.5 seconds
            )
            .run { context ->
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify complex Duration parsing
                assertEquals(Duration.ofSeconds(30), properties.claimIdleTimeout)
                assertEquals(Duration.ofMillis(1500), properties.pollTimeout)

                // Verify all beans are still created with complex durations
                assertTrue(context.containsBean("eventStore"))
                assertTrue(context.containsBean("eventConsumer"))

                logger.debug("[DEBUG_LOG] Complex Duration configuration test passed")
            }
    }

    @Test
    @DisplayName("Should handle special property combinations")
    fun `should handle special property combinations`() {
        contextRunner
            .withPropertyValues(
                "redis.event-store.host=redis.example.com",  // External host
                "redis.event-store.password=",  // Empty password (no auth)
                "redis.event-store.stream-prefix=custom:",  // Custom prefix
                "redis.event-store.use-pooling=false",  // Disable pooling
                "redis.event-store.create-consumer-group-if-not-exists=false"  // Manual group management
            )
            .run { context ->
                val properties = context.getBean(RedisEventStoreProperties::class.java)
                assertNotNull(properties)

                // Verify special configuration combinations
                assertEquals("redis.example.com", properties.host)
                assertEquals("", properties.password)
                assertEquals("custom:", properties.streamPrefix)
                assertFalse(properties.usePooling)
                assertFalse(properties.createConsumerGroupIfNotExists)

                // Beans should still be created with special combinations
                assertTrue(context.containsBean("eventStoreRedisConnectionFactory"))
                assertTrue(context.containsBean("eventStore"))

                logger.debug("[DEBUG_LOG] Special property combinations test passed")
            }
    }
}
