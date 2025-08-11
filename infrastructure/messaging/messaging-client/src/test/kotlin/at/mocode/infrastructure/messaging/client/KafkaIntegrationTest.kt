// KafkaIntegrationTest.kt

package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.test.StepVerifier
import java.util.*

@Testcontainers
class KafkaIntegrationTest {

    companion object {
        @Container
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
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
    fun `publishEvent should send a message that can be received`() {
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

        // Der Mono, der das nächste empfangene Ereignis darstellt
        val receivedEvent = KafkaReceiver.create(receiverOptions)
            .receive()
            .next() // Nimm nur das erste Ereignis
            .map { it.value() } // Extrahiere den Wert (unsere TestEvent-Instanz)

        // Der Mono, der die Sende-Aktion darstellt
        val sendAction = kafkaEventPublisher.publishEvent(testTopic, testKey, testEvent)

        // KORREKTUR: Kombiniere die Sende-Aktion und die Empfangs-Erwartung in einem StepVerifier.
        // Die `then` Methode stellt sicher, dass erst die Sende-Aktion abgeschlossen wird,
        // bevor der `receivedEvent` Mono abonniert und verifiziert wird.
        StepVerifier.create(sendAction.then(receivedEvent))
            .expectNext(testEvent) // Erwarte, dass unser Test-Event ankommt
            .verifyComplete()      // Schließe die Überprüfung ab
    }

    data class TestEvent(val message: String)
}
