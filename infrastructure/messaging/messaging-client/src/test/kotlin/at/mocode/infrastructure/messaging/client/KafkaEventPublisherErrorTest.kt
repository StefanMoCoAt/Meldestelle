package at.mocode.infrastructure.messaging.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult
import reactor.test.StepVerifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaEventPublisherErrorTest {

    private lateinit var mockTemplate: ReactiveKafkaProducerTemplate<String, Any>
    private lateinit var publisher: KafkaEventPublisher

    @BeforeEach
    fun setUp() {
        mockTemplate = mockk<ReactiveKafkaProducerTemplate<String, Any>>()
        publisher = KafkaEventPublisher(mockTemplate)
    }

    @Test
    fun `should publish single event successfully`() {
        val testEvent = TestEvent("data")
        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockRecordMetadata.partition() } returns 0
        every { mockRecordMetadata.offset() } returns 0L
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        every { mockTemplate.send("test-topic", "key", testEvent) } returns Mono.just(mockResult)

        StepVerifier.create(publisher.publishEventReactive("test-topic", "key", testEvent))
            .expectNext(Unit)
            .verifyComplete()

        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle serialization errors without retry`() {
        val testEvent = TestEvent("data")

        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(RuntimeException("Serialization failed"))

        StepVerifier.create(publisher.publishEventReactive("test-topic", "key", testEvent))
            .verifyError(RuntimeException::class.java)

        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle authentication errors without retry`() {
        val testEvent = TestEvent("data")

        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(RuntimeException("Authentication failed"))

        StepVerifier.create(publisher.publishEventReactive("test-topic", "key", testEvent))
            .verifyError(RuntimeException::class.java)

        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle empty batch gracefully`() {
        val emptyEvents = emptyList<Pair<String?, Any>>()

        StepVerifier.create(publisher.publishEventsReactive("test-topic", emptyEvents))
            .verifyComplete()

        verify(exactly = 0) { mockTemplate.send(any(), any(), any()) }
    }

    @Test
    fun `should publish batch events successfully`() {
        val events = listOf(
            "key1" to TestEvent("message1"),
            "key2" to TestEvent("message2")
        )

        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockRecordMetadata.partition() } returns 0
        every { mockRecordMetadata.offset() } returns 0L
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        every { mockTemplate.send("test-topic", "key1", any()) } returns Mono.just(mockResult)
        every { mockTemplate.send("test-topic", "key2", any()) } returns Mono.just(mockResult)

        StepVerifier.create(publisher.publishEventsReactive("test-topic", events))
            .expectNextCount(2)
            .verifyComplete()

        verify(exactly = 1) { mockTemplate.send("test-topic", "key1", any()) }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key2", any()) }
    }

    data class TestEvent(val message: String)
}
