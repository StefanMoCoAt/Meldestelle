package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import kotlinx.coroutines.test.runTest
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.test.StepVerifier
import java.util.*

@Testcontainers
class KafkaIntegrationTest {

    companion object {
        @Container
        private val kafkaContainer = KafkaContainer(
            DockerImageName.parse("apache/kafka:4.1.1")
        )
    }

    private lateinit var kafkaEventPublisher: KafkaEventPublisher
    private lateinit var producerFactory: DefaultKafkaProducerFactory<String, Any>
    private val testTopic = "test-topic-${UUID.randomUUID()}"

    @BeforeEach
    fun setUp() {
        val kafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
        }
        producerFactory = kafkaConfig.producerFactory()

        val reactiveKafkaConfig = ReactiveKafkaConfig(kafkaConfig)
        val reactiveTemplate = reactiveKafkaConfig.reactiveKafkaProducerTemplate()
        kafkaEventPublisher = KafkaEventPublisher(reactiveTemplate)
    }

    @AfterEach
    fun tearDown() {
        producerFactory.destroy()
    }

    @Test
    fun `publishEvent should send a message that can be received`() = runTest {
        // Arrange
        val testKey = "test-key"
        val testEvent = TestEvent("Test Message")

        // Use the same KafkaConfig for consistent and secure configuration
        val testKafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            // For tests, we need to trust the test package
            trustedPackages = "at.mocode.*"
        }

        val consumerProps = testKafkaConfig.consumerConfigs("test-group-${UUID.randomUUID()}")

        val jsonValueDeserializer = JsonDeserializer(TestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }
        val receiverOptions = ReceiverOptions.create<String, TestEvent>(consumerProps)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonValueDeserializer)
            .subscription(listOf(testTopic))

        // The Mono that represents the next received event
        val receivedEvent = KafkaReceiver.create(receiverOptions)
            .receive()
            .next() // Take only the first event
            .map { it.value() } // Extract the value (our TestEvent instance)

        // Execute the send action and verify success
        val publishResult = kafkaEventPublisher.publishEvent(testTopic, testKey, testEvent)
        assert(publishResult.isSuccess) { "Expected successful publish result" }

        // Verify that the message can be received
        StepVerifier.create(receivedEvent)
            .expectNext(testEvent) // Expect that our test event arrives
            .verifyComplete()      // Complete the verification
    }

    @Test
    fun `publishEvents should send batch messages that can be received`() = runTest {
        // Arrange
        val batchSize = 10
        val eventBatch = (1..batchSize).map { i ->
            "batch-key-$i" to TestEvent("Batch message $i")
        }

        // Consumer setup
        val testKafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            trustedPackages = "at.mocode.*"
        }

        val consumerProps = testKafkaConfig.consumerConfigs("batch-test-group-${UUID.randomUUID()}")
        val jsonValueDeserializer = JsonDeserializer(TestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }
        val receiverOptions = ReceiverOptions.create<String, TestEvent>(consumerProps)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonValueDeserializer)
            .subscription(listOf(testTopic))

        // Collect received events
        val receivedEvents = KafkaReceiver.create(receiverOptions)
            .receive()
            .take(batchSize.toLong())
            .map { it.value() }
            .collectList()

        // Send batch and verify success
        val publishResult = kafkaEventPublisher.publishEvents(testTopic, eventBatch)
        assert(publishResult.isSuccess) { "Expected successful batch publish result" }
        assert(publishResult.getOrNull()?.size == batchSize) { "Expected $batchSize successful operations" }

        // Verify reception
        StepVerifier.create(receivedEvents)
            .expectNextMatches { events ->
                events.size == batchSize && events.all { it.message.startsWith("Batch message") }
            }
            .verifyComplete()
    }

    @Test
    fun `should handle multiple consumers on same topic`() = runTest {
        val testEvent = TestEvent("Multi-consumer message")
        val testKey = "multi-consumer-key"

        // Setup two consumers with different group IDs
        val testKafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            trustedPackages = "at.mocode.*"
        }

        val consumer1Props = testKafkaConfig.consumerConfigs("consumer-group-1-${UUID.randomUUID()}")
        val consumer2Props = testKafkaConfig.consumerConfigs("consumer-group-2-${UUID.randomUUID()}")

        val jsonDeserializer1 = JsonDeserializer(TestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }
        val jsonDeserializer2 = JsonDeserializer(TestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }

        val receiverOptions1 = ReceiverOptions.create<String, TestEvent>(consumer1Props)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonDeserializer1)
            .subscription(listOf(testTopic))

        val receiverOptions2 = ReceiverOptions.create<String, TestEvent>(consumer2Props)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonDeserializer2)
            .subscription(listOf(testTopic))

        val consumer1Event = KafkaReceiver.create(receiverOptions1)
            .receive()
            .next()
            .map { it.value() }

        val consumer2Event = KafkaReceiver.create(receiverOptions2)
            .receive()
            .next()
            .map { it.value() }

        // Execute the send action and verify success
        val publishResult = kafkaEventPublisher.publishEvent(testTopic, testKey, testEvent)
        assert(publishResult.isSuccess) { "Expected successful publish result" }

        // Both consumers should receive the same message (different groups)
        StepVerifier.create(consumer1Event.zipWith(consumer2Event))
            .expectNextMatches { tuple ->
                tuple.t1 == testEvent && tuple.t2 == testEvent
            }
            .verifyComplete()
    }

    @Test
    fun `should handle different event types in integration scenario`() = runTest {
        val complexEvent = ComplexTestEvent(
            id = 123,
            name = "Integration Test",
            metadata = mapOf("type" to "complex", "version" to "1.0"),
            timestamp = System.currentTimeMillis()
        )

        val testKafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            trustedPackages = "at.mocode.*"
        }

        val consumerProps = testKafkaConfig.consumerConfigs("complex-test-group-${UUID.randomUUID()}")
        val jsonValueDeserializer = JsonDeserializer(ComplexTestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }
        val receiverOptions = ReceiverOptions.create<String, ComplexTestEvent>(consumerProps)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonValueDeserializer)
            .subscription(listOf(testTopic))

        val receivedEvent = KafkaReceiver.create(receiverOptions)
            .receive()
            .next()
            .map { it.value() }

        // Execute the send action and verify success
        val publishResult = kafkaEventPublisher.publishEvent(testTopic, "complex-key", complexEvent)
        assert(publishResult.isSuccess) { "Expected successful publish result" }

        // Verify that the complex event can be received
        StepVerifier.create(receivedEvent)
            .expectNext(complexEvent)
            .verifyComplete()
    }

    @Test
    fun `should maintain message ordering within partition`() = runTest {
        val partitionKey = "ordered-messages"
        val messageCount = 5
        val orderedEvents = (1..messageCount).map { i ->
            partitionKey to TestEvent("Ordered message $i")
        }

        val testKafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            trustedPackages = "at.mocode.*"
        }

        val consumerProps = testKafkaConfig.consumerConfigs("ordering-test-group-${UUID.randomUUID()}")
        val jsonValueDeserializer = JsonDeserializer(TestEvent::class.java).apply {
            addTrustedPackages(testKafkaConfig.trustedPackages)
            setUseTypeHeaders(false)
        }
        val receiverOptions = ReceiverOptions.create<String, TestEvent>(consumerProps)
            .withKeyDeserializer(StringDeserializer())
            .withValueDeserializer(jsonValueDeserializer)
            .subscription(listOf(testTopic))

        val receivedEvents = KafkaReceiver.create(receiverOptions)
            .receive()
            .take(messageCount.toLong())
            .map { it.value() }
            .collectList()

        // Send ordered events and verify success
        val publishResult = kafkaEventPublisher.publishEvents(testTopic, orderedEvents)
        assert(publishResult.isSuccess) { "Expected successful batch publish result" }
        assert(publishResult.getOrNull()?.size == messageCount) { "Expected $messageCount successful operations" }

        // Verify message ordering is maintained
        StepVerifier.create(receivedEvents)
            .expectNextMatches { events ->
                events.size == messageCount &&
                events.mapIndexed { index, event ->
                    event.message == "Ordered message ${index + 1}"
                }.all { it }
            }
            .verifyComplete()
    }

    @Test
    fun `should handle empty batch gracefully in integration test`() = runTest {
        val emptyBatch = emptyList<Pair<String?, Any>>()

        val publishResult = kafkaEventPublisher.publishEvents(testTopic, emptyBatch)
        assert(publishResult.isSuccess) { "Expected successful result for empty batch" }
        assert(publishResult.getOrNull()?.isEmpty() == true) { "Expected empty result list" }
    }

    data class TestEvent(val message: String)

    data class ComplexTestEvent(
        val id: Int,
        val name: String,
        val metadata: Map<String, String>,
        val timestamp: Long
    )
}
