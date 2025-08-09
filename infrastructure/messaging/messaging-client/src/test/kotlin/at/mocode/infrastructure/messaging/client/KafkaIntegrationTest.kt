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
        producerFactory = kafkaConfig.producerFactory() as DefaultKafkaProducerFactory<String, Any>

        val reactiveKafkaConfig = ReactiveKafkaConfig()
        val reactiveTemplate = reactiveKafkaConfig.reactiveKafkaProducerTemplate(producerFactory)
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

        val consumerProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "test-group-${UUID.randomUUID()}",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )
        val receiverOptions = ReceiverOptions.create<String, TestEvent>(consumerProps).subscription(listOf(testTopic))

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
