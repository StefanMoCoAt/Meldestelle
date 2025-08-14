package at.mocode.infrastructure.messaging.client

import io.mockk.clearMocks
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
import java.io.IOException
import java.net.ConnectException
import java.util.concurrent.TimeoutException

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
    fun `should retry on transient timeout errors`() {
        val testEvent = TestEvent("data")
        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        // The first call fails with timeout, the second succeeds
        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(TimeoutException("Connection timeout")) andThen
            Mono.just(mockResult)

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyComplete()

        verify(exactly = 2) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should retry on connection errors`() {
        val testEvent = TestEvent("data")
        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        // First call fails with connection error, second succeeds
        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(ConnectException("Connection refused")) andThen
            Mono.just(mockResult)

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyComplete()

        verify(exactly = 2) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should retry on IO errors`() {
        val testEvent = TestEvent("data")
        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        // First call fails with IOException, second succeeds
        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(IOException("Network error")) andThen
            Mono.just(mockResult)

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyComplete()

        verify(exactly = 2) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should not retry on serialization errors`() {
        val testEvent = TestEvent("data")

        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(RuntimeException("Serialization failed"))

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyError(RuntimeException::class.java)

        // Should only try once, no retries for serialization errors
        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should not retry on authentication errors`() {
        val testEvent = TestEvent("data")

        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(RuntimeException("Authentication failed"))

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyError(RuntimeException::class.java)

        // Should only try once, no retries for auth errors
        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should exhaust retries and fail after maximum attempts`() {
        val testEvent = TestEvent("data")

        // Always fail with retryable error
        every { mockTemplate.send("test-topic", "key", testEvent) } returns
            Mono.error(TimeoutException("Connection timeout"))

        StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
            .verifyError(TimeoutException::class.java)

        // Should try 1 initial + 3 retries = 4 times total
        verify(exactly = 4) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle batch publishing with partial failures`() {
        val events = listOf(
            "key1" to TestEvent("success1"),
            "key2" to TestEvent("failure"),
            "key3" to TestEvent("success2")
        )

        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        // First and third events succeed, second fails
        every { mockTemplate.send("test-topic", "key1", any()) } returns Mono.just(mockResult)
        every { mockTemplate.send("test-topic", "key2", any()) } returns
            Mono.error(RuntimeException("Serialization failed"))
        every { mockTemplate.send("test-topic", "key3", any()) } returns Mono.just(mockResult)

        StepVerifier.create(publisher.publishEvents("test-topic", events))
            .expectNextCount(2) // Should complete 2 successful sends
            .verifyComplete()

        // Verify all events were attempted
        verify(exactly = 1) { mockTemplate.send("test-topic", "key1", any()) }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key2", any()) }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key3", any()) }
    }

    @Test
    fun `should handle batch publishing with retryable failures`() {
        val events = listOf(
            "key1" to TestEvent("success"),
            "key2" to TestEvent("retry-then-success")
        )

        val mockResult = mockk<SenderResult<Void>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockResult.recordMetadata() } returns mockRecordMetadata

        // First event succeeds immediately
        every { mockTemplate.send("test-topic", "key1", any()) } returns Mono.just(mockResult)

        // Second event fails first time, succeeds on retry
        every { mockTemplate.send("test-topic", "key2", any()) } returns
            Mono.error(TimeoutException("Connection timeout")) andThen
            Mono.just(mockResult)

        StepVerifier.create(publisher.publishEvents("test-topic", events))
            .expectNextCount(2) // Should complete both events
            .verifyComplete()

        // First event called once, second event called twice (initial + retry)
        verify(exactly = 1) { mockTemplate.send("test-topic", "key1", any()) }
        verify(exactly = 2) { mockTemplate.send("test-topic", "key2", any()) }
    }

    @Test
    fun `should handle empty batch gracefully`() {
        val emptyEvents = emptyList<Pair<String?, Any>>()

        StepVerifier.create(publisher.publishEvents("test-topic", emptyEvents))
            .verifyComplete()

        // Should not call the template at all
        verify(exactly = 0) { mockTemplate.send(any(), any(), any()) }
    }

    @Test
    fun `should identify retryable exceptions correctly`() {
        // Test the private isRetryableException method through behavior
        val testEvent = TestEvent("data")

        // Test various error messages that should be retryable
        val retryableErrors = listOf(
            RuntimeException("timeout occurred"),
            RuntimeException("connection refused"),
            RuntimeException("network unreachable"),
            TimeoutException("Request timeout"),
            ConnectException("Connection failed"),
            IOException("I/O error")
        )

        retryableErrors.forEach { error ->
            clearMocks(mockTemplate)
            every { mockTemplate.send("test-topic", "key", testEvent) } returns
                Mono.error(error) andThen Mono.error(error) // Fail twice to test retry

            StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
                .verifyError()

            // Should retry (at least 2 calls)
            verify(atLeast = 2) { mockTemplate.send("test-topic", "key", testEvent) }
        }
    }

    @Test
    fun `should identify non-retryable exceptions correctly`() {
        val testEvent = TestEvent("data")

        // Test various error messages that should NOT be retryable
        val nonRetryableErrors = listOf(
            RuntimeException("serialization error"),
            RuntimeException("deserialization failed"),
            RuntimeException("authentication failed"),
            RuntimeException("authorization denied")
        )

        nonRetryableErrors.forEach { error ->
            clearMocks(mockTemplate)
            every { mockTemplate.send("test-topic", "key", testEvent) } returns Mono.error(error)

            StepVerifier.create(publisher.publishEvent("test-topic", "key", testEvent))
                .verifyError()

            // Should NOT retry (exactly 1 call)
            verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
        }
    }

    data class TestEvent(val message: String)
}
