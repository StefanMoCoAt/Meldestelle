package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaSecurityTest {

    @Test
    fun `should configure trusted packages correctly for JSON deserializer`() {
        val config = KafkaConfig().apply {
            trustedPackages = "at.mocode.*,com.example.*"
        }

        val consumerConfigs = config.consumerConfigs("security-test-group")

        // Verify trusted packages configuration
        assertThat(consumerConfigs[JsonDeserializer.TRUSTED_PACKAGES]).isEqualTo("at.mocode.*,com.example.*")
        assertThat(consumerConfigs[JsonDeserializer.USE_TYPE_INFO_HEADERS]).isEqualTo(false)
    }

    @Test
    fun `should reject empty trusted packages configuration`() {
        val config = KafkaConfig()

        assertThrows<IllegalArgumentException> {
            config.trustedPackages = ""
        }

        assertThrows<IllegalArgumentException> {
            config.trustedPackages = "   "
        }
    }

    @Test
    fun `should validate trusted packages with various formats`() {
        val config = KafkaConfig()

        // Valid trusted package formats
        val validPackages = listOf(
            "at.mocode.*",
            "at.mocode.*,com.example.*",
            "java.lang.*,java.util.*",
            "com.company.specific.Package",
            "org.springframework.*,at.mocode.*,com.test.*"
        )

        validPackages.forEach { packages ->
            assertDoesNotThrow {
                config.trustedPackages = packages
                assertThat(config.trustedPackages).isEqualTo(packages)
            }
        }
    }

    @Test
    fun `should configure security features when enabled`() {
        val config = KafkaConfig().apply {
            enableSecurityFeatures = true
        }

        val producerConfigs = config.producerConfigs()
        val consumerConfigs = config.consumerConfigs("secure-group")

        // Verify security-related producer configurations
        assertThat(producerConfigs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG]).isEqualTo(true)
        assertThat(producerConfigs[ProducerConfig.ACKS_CONFIG]).isEqualTo("all")
        assertThat(producerConfigs[ProducerConfig.RETRIES_CONFIG]).isEqualTo(3)

        // Verify security-related consumer configurations
        assertThat(consumerConfigs[JsonDeserializer.TRUSTED_PACKAGES]).isEqualTo("at.mocode.*")
        assertThat(consumerConfigs[JsonDeserializer.USE_TYPE_INFO_HEADERS]).isEqualTo(false)
        assertThat(consumerConfigs[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG]).isEqualTo(false)
    }

    @Test
    fun `should configure security features when disabled`() {
        val config = KafkaConfig().apply {
            enableSecurityFeatures = false // Explicitly disable
        }

        val producerConfigs = config.producerConfigs()
        val consumerConfigs = config.consumerConfigs("non-secure-group")

        // Even when disabled, core security features should still be present
        // This ensures baseline security is maintained
        assertThat(producerConfigs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG]).isEqualTo(true)
        assertThat(producerConfigs[ProducerConfig.ACKS_CONFIG]).isEqualTo("all")
        assertThat(consumerConfigs[JsonDeserializer.TRUSTED_PACKAGES]).isEqualTo("at.mocode.*")
    }

    @Test
    fun `should prevent JSON type header usage for security`() {
        val config = KafkaConfig()
        val producerConfigs = config.producerConfigs()
        val consumerConfigs = config.consumerConfigs("header-test-group")

        // Type headers should be disabled to prevent deserialization attacks
        assertThat(producerConfigs[JsonSerializer.ADD_TYPE_INFO_HEADERS]).isEqualTo(false)
        assertThat(consumerConfigs[JsonDeserializer.USE_TYPE_INFO_HEADERS]).isEqualTo(false)
    }

    @Test
    fun `should create secure JSON deserializer for consumer`() {
        val config = KafkaConfig().apply {
            trustedPackages = "at.mocode.*,com.test.*"
        }

        val consumer = KafkaEventConsumer(config)

        // Test that consumer can be created with security configuration
        assertThat(consumer).isNotNull

        // Test that reactive streams can be created (they use secure deserializer internally)
        assertDoesNotThrow {
            val flux = consumer.receiveEvents<SecureTestEvent>("secure-topic")
            assertThat(flux).isNotNull
        }
    }

    @Test
    fun `should handle multiple trusted package patterns`() {
        val config = KafkaConfig().apply {
            trustedPackages = "at.mocode.domain.*,at.mocode.events.*,com.example.secure.*"
        }

        val consumerConfigs = config.consumerConfigs("multi-pattern-group")

        assertThat(consumerConfigs[JsonDeserializer.TRUSTED_PACKAGES])
            .isEqualTo("at.mocode.domain.*,at.mocode.events.*,com.example.secure.*")
    }

    @Test
    fun `should enforce manual commit for better security control`() {
        val config = KafkaConfig()
        val consumerConfigs = config.consumerConfigs("manual-commit-group")

        // Auto-commit should be disabled for better control over message processing
        assertThat(consumerConfigs[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG]).isEqualTo(false)

        // Session and heartbeat timeouts should be configured for security
        assertThat(consumerConfigs[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG]).isEqualTo(30000)
        assertThat(consumerConfigs[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG]).isEqualTo(3000)
    }

    @Test
    fun `should configure connection security settings`() {
        val config = KafkaConfig()
        val consumerConfigs = config.consumerConfigs("connection-security-group")

        // Connection security settings
        assertThat(consumerConfigs[ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG]).isEqualTo(540000)
        assertThat(consumerConfigs[ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG]).isEqualTo(50)
        assertThat(consumerConfigs[ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG]).isEqualTo(1000)
    }

    @Test
    fun `should validate connection pool size for security`() {
        val config = KafkaConfig()

        // Valid connection pool sizes
        assertDoesNotThrow { config.connectionPoolSize = 1 }
        assertDoesNotThrow { config.connectionPoolSize = 5 }
        assertDoesNotThrow { config.connectionPoolSize = 50 }

        // Invalid connection pool sizes (security risk - too many connections)
        assertThrows<IllegalArgumentException> { config.connectionPoolSize = 0 }
        assertThrows<IllegalArgumentException> { config.connectionPoolSize = -1 }
    }

    @Test
    fun `should create producer factory with secure configuration`() {
        val config = KafkaConfig().apply {
            trustedPackages = "at.mocode.*"
            enableSecurityFeatures = true
        }

        val producerFactory = config.producerFactory()

        // Verify producer factory is created successfully
        assertThat(producerFactory).isNotNull

        // Test creating a producer
        assertDoesNotThrow {
            val producer = producerFactory.createProducer()
            assertThat(producer).isNotNull
        }

        // Verify secure configuration is applied
        val configs = producerFactory.configurationProperties
        assertThat(configs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG]).isEqualTo(true)
        assertThat(configs[JsonSerializer.ADD_TYPE_INFO_HEADERS]).isEqualTo(false)
    }

    @Test
    fun `should handle security configuration for different environments`() {
        // Development environment
        val devConfig = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            trustedPackages = "at.mocode.*,com.test.*"
            enableSecurityFeatures = true
        }

        // Production environment
        val prodConfig = KafkaConfig().apply {
            bootstrapServers = "prod-kafka:9092"
            trustedPackages = "at.mocode.*" // More restrictive
            enableSecurityFeatures = true
            connectionPoolSize = 20
        }

        // Both configurations should be valid
        assertDoesNotThrow {
            KafkaEventConsumer(devConfig)
            KafkaEventPublisher(ReactiveKafkaConfig(devConfig).reactiveKafkaProducerTemplate())
        }

        assertDoesNotThrow {
            KafkaEventConsumer(prodConfig)
            KafkaEventPublisher(ReactiveKafkaConfig(prodConfig).reactiveKafkaProducerTemplate())
        }
    }

    @Test
    fun `should validate group ID format for security`() {
        val config = KafkaConfig()

        // Valid group ID prefixes
        val validPrefixes = listOf(
            "secure-consumer",
            "production.consumer",
            "dev_consumer",
            "consumer-123"
        )

        validPrefixes.forEach { prefix ->
            assertDoesNotThrow {
                config.defaultGroupIdPrefix = prefix
                assertThat(config.defaultGroupIdPrefix).isEqualTo(prefix)
            }
        }

        // Invalid group ID prefixes (potential security issues)
        val invalidPrefixes = listOf(
            "", // Empty
            "   ", // Whitespace only
            "invalid@consumer", // Special characters
            "consumer with spaces",
            "consumer/with/slashes",
            "consumer#hash"
        )

        invalidPrefixes.forEach { prefix ->
            assertThrows<IllegalArgumentException> {
                config.defaultGroupIdPrefix = prefix
            }
        }
    }

    @Test
    fun `should configure serialization security`() {
        val config = KafkaConfig().apply {
            trustedPackages = "at.mocode.*,com.secure.*"
        }

        val producerConfigs = config.producerConfigs()
        val consumerConfigs = config.consumerConfigs("serialization-security-group")

        // Producer serialization security
        assertThat(producerConfigs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG].toString())
            .isEqualTo("class org.apache.kafka.common.serialization.StringSerializer")
        assertThat(producerConfigs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG].toString())
            .isEqualTo("class org.springframework.kafka.support.serializer.JsonSerializer")
        assertThat(producerConfigs[JsonSerializer.ADD_TYPE_INFO_HEADERS]).isEqualTo(false)

        // Consumer deserialization security
        assertThat(consumerConfigs[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG].toString())
            .isEqualTo("class org.apache.kafka.common.serialization.StringDeserializer")
        assertThat(consumerConfigs[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG].toString())
            .isEqualTo("class org.springframework.kafka.support.serializer.JsonDeserializer")
        assertThat(consumerConfigs[JsonDeserializer.TRUSTED_PACKAGES]).isEqualTo("at.mocode.*,com.secure.*")
        assertThat(consumerConfigs[JsonDeserializer.USE_TYPE_INFO_HEADERS]).isEqualTo(false)
    }

    @Test
    fun `should provide secure defaults`() {
        val config = KafkaConfig() // Use default values

        // Verify secure defaults
        assertThat(config.trustedPackages).isEqualTo("at.mocode.*")
        assertThat(config.enableSecurityFeatures).isEqualTo(true)
        assertThat(config.connectionPoolSize).isEqualTo(10)
        assertThat(config.defaultGroupIdPrefix).isEqualTo("messaging-client")

        // Verify secure configurations are applied with defaults
        val producerConfigs = config.producerConfigs()
        val consumerConfigs = config.consumerConfigs()

        assertThat(producerConfigs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG]).isEqualTo(true)
        assertThat(producerConfigs[JsonSerializer.ADD_TYPE_INFO_HEADERS]).isEqualTo(false)
        assertThat(consumerConfigs[JsonDeserializer.USE_TYPE_INFO_HEADERS]).isEqualTo(false)
        assertThat(consumerConfigs[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG]).isEqualTo(false)
    }

    data class SecureTestEvent(
        val data: String,
        val timestamp: Long = System.currentTimeMillis()
    )
}
