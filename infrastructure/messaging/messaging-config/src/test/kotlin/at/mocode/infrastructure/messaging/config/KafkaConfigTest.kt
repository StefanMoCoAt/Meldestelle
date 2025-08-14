package at.mocode.infrastructure.messaging.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaConfigTest {

    @Test
    fun `should validate bootstrap servers format`() {
        val config = KafkaConfig()

        // Valid formats
        assertDoesNotThrow { config.bootstrapServers = "localhost:9092" }
        assertDoesNotThrow { config.bootstrapServers = "PLAINTEXT://localhost:9092" }
        assertDoesNotThrow { config.bootstrapServers = "host1:9092,host2:9092" }
        assertDoesNotThrow { config.bootstrapServers = "PLAINTEXT://host1:9092,PLAINTEXT://host2:9092" }
        assertDoesNotThrow { config.bootstrapServers = "kafka.example.com:9092" }
        assertDoesNotThrow { config.bootstrapServers = "kafka-cluster-01.internal:9092" }

        // Invalid formats
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "" }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "   " }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "invalid-format" }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "localhost" }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = ":9092" }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "localhost:" }
        assertThrows<IllegalArgumentException> { config.bootstrapServers = "localhost:abc" }
    }

    @Test
    fun `should validate group ID prefix`() {
        val config = KafkaConfig()

        // Valid prefixes
        assertDoesNotThrow { config.defaultGroupIdPrefix = "valid-prefix_123" }
        assertDoesNotThrow { config.defaultGroupIdPrefix = "messaging-client" }
        assertDoesNotThrow { config.defaultGroupIdPrefix = "test.group.id" }
        assertDoesNotThrow { config.defaultGroupIdPrefix = "simple123" }

        // Invalid prefixes
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "" }
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "   " }
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "invalid@prefix" }
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "invalid#prefix" }
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "invalid prefix" }
        assertThrows<IllegalArgumentException> { config.defaultGroupIdPrefix = "invalid/prefix" }
    }

    @Test
    fun `should validate trusted packages`() {
        val config = KafkaConfig()

        // Valid trusted packages
        assertDoesNotThrow { config.trustedPackages = "at.mocode.*,com.example.*" }
        assertDoesNotThrow { config.trustedPackages = "at.mocode.*" }
        assertDoesNotThrow { config.trustedPackages = "com.example.specific.Package" }
        assertDoesNotThrow { config.trustedPackages = "java.lang.*,java.util.*" }

        // Invalid trusted packages
        assertThrows<IllegalArgumentException> { config.trustedPackages = "" }
        assertThrows<IllegalArgumentException> { config.trustedPackages = "   " }
    }

    @Test
    fun `should validate connection pool size`() {
        val config = KafkaConfig()

        // Valid pool sizes
        assertDoesNotThrow { config.connectionPoolSize = 1 }
        assertDoesNotThrow { config.connectionPoolSize = 5 }
        assertDoesNotThrow { config.connectionPoolSize = 10 }
        assertDoesNotThrow { config.connectionPoolSize = 100 }

        // Invalid pool sizes
        assertThrows<IllegalArgumentException> { config.connectionPoolSize = 0 }
        assertThrows<IllegalArgumentException> { config.connectionPoolSize = -1 }
        assertThrows<IllegalArgumentException> { config.connectionPoolSize = -10 }
    }

    @Test
    fun `should have default values set correctly`() {
        val config = KafkaConfig()

        assertThat(config.bootstrapServers).isEqualTo("localhost:9092")
        assertThat(config.defaultGroupIdPrefix).isEqualTo("messaging-client")
        assertThat(config.trustedPackages).isEqualTo("at.mocode.*")
        assertThat(config.enableSecurityFeatures).isEqualTo(true)
        assertThat(config.connectionPoolSize).isEqualTo(10)
    }

    @Test
    fun `should generate valid producer configs`() {
        val config = KafkaConfig()
        val producerConfigs = config.producerConfigs()

        // Verify essential producer configuration
        assertThat(producerConfigs["bootstrap.servers"]).isEqualTo("localhost:9092")
        assertThat(producerConfigs["key.serializer"]).isEqualTo(org.apache.kafka.common.serialization.StringSerializer::class.java)
        assertThat(producerConfigs["value.serializer"]).isEqualTo(org.springframework.kafka.support.serializer.JsonSerializer::class.java)
        assertThat(producerConfigs["acks"]).isEqualTo("all")
        assertThat(producerConfigs["enable.idempotence"]).isEqualTo(true)
    }

    @Test
    fun `should generate valid consumer configs with custom group ID`() {
        val config = KafkaConfig()
        val customGroupId = "test-group-123"
        val consumerConfigs = config.consumerConfigs(customGroupId)

        // Verify essential consumer configuration
        assertThat(consumerConfigs["bootstrap.servers"]).isEqualTo("localhost:9092")
        assertThat(consumerConfigs["group.id"]).isEqualTo(customGroupId)
        assertThat(consumerConfigs["key.deserializer"]).isEqualTo(org.apache.kafka.common.serialization.StringDeserializer::class.java)
        assertThat(consumerConfigs["value.deserializer"]).isEqualTo(org.springframework.kafka.support.serializer.JsonDeserializer::class.java)
        assertThat(consumerConfigs["spring.json.trusted.packages"]).isEqualTo("at.mocode.*")
        assertThat(consumerConfigs["auto.offset.reset"]).isEqualTo("earliest")
        assertThat(consumerConfigs["enable.auto.commit"]).isEqualTo(false)
    }

    @Test
    fun `should generate unique consumer configs when no group ID provided`() {
        val config = KafkaConfig()
        val consumerConfigs1 = config.consumerConfigs()
        val consumerConfigs2 = config.consumerConfigs()

        // Group IDs should be different (timestamp-based)
        val groupId1 = consumerConfigs1["group.id"].toString()
        val groupId2 = consumerConfigs2["group.id"].toString()

        assertThat(groupId1).isNotEqualTo(groupId2)
        assertThat(groupId1).startsWith("messaging-client-")
        assertThat(groupId2).startsWith("messaging-client-")
    }

    @Test
    fun `should create producer factory with correct configuration`() {
        val config = KafkaConfig()
        val producerFactory = config.producerFactory()

        assertDoesNotThrow { producerFactory.createProducer() }
        assertThat(producerFactory.configurationProperties["bootstrap.servers"]).isEqualTo("localhost:9092")
    }
}
