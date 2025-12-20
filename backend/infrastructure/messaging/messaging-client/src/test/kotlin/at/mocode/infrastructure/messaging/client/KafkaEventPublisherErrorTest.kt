package at.mocode.infrastructure.messaging.client

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaEventPublisherErrorTest {

    private lateinit var mockTemplate: KafkaTemplate<String, Any>
    private lateinit var publisher: KafkaEventPublisher

    @BeforeEach
    fun setUp() {
        mockTemplate = mockk<KafkaTemplate<String, Any>>()
        publisher = KafkaEventPublisher(mockTemplate)
    }

    @Test
    fun `should publish single event successfully`() = runTest {
        val testEvent = TestEvent("data")
        val mockResult = mockk<SendResult<String, Any>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockRecordMetadata.partition() } returns 0
        every { mockRecordMetadata.offset() } returns 0L
        every { mockResult.recordMetadata } returns mockRecordMetadata
        every { mockTemplate.send("test-topic", "key", testEvent) } returns CompletableFuture.completedFuture(mockResult)

        val result = publisher.publishEvent("test-topic", "key", testEvent)

        assert(result.isSuccess) { "Expected successful result" }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle serialization errors without retry`() = runTest {
        val testEvent = TestEvent("data")

        val failedFuture = CompletableFuture<SendResult<String, Any>>()
        failedFuture.completeExceptionally(RuntimeException("Serialization failed"))
        every { mockTemplate.send("test-topic", "key", testEvent) } returns failedFuture

        val result = publisher.publishEvent("test-topic", "key", testEvent)

        assert(result.isFailure) { "Expected failed result" }
        assert(result.exceptionOrNull() is MessagingError.SerializationError) { "Expected MessagingError.SerializationError" }
        assert(result.exceptionOrNull()?.message?.contains("Serialization failed") == true) { "Expected specific error message" }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle authentication errors without retry`() = runTest {
        val testEvent = TestEvent("data")

        val authFailed = CompletableFuture<SendResult<String, Any>>()
        authFailed.completeExceptionally(RuntimeException("Authentication failed"))
        every { mockTemplate.send("test-topic", "key", testEvent) } returns authFailed

        val result = publisher.publishEvent("test-topic", "key", testEvent)

        assert(result.isFailure) { "Expected failed result" }
        assert(result.exceptionOrNull() is MessagingError.AuthenticationError) { "Expected MessagingError.AuthenticationError" }
        assert(result.exceptionOrNull()?.message?.contains("Authentication failed") == true) { "Expected specific error message" }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key", testEvent) }
    }

    @Test
    fun `should handle empty batch gracefully`() = runTest {
        val emptyEvents = emptyList<Pair<String?, Any>>()

        val result = publisher.publishEvents("test-topic", emptyEvents)

        assert(result.isSuccess) { "Expected successful result for empty batch" }
        assert(result.getOrNull()?.isEmpty() == true) { "Expected empty result list" }
        verify(exactly = 0) { mockTemplate.send(any(), any(), any()) }
    }

    @Test
    fun `should publish batch events successfully`() = runTest {
        val events = listOf(
            "key1" to TestEvent("message1"),
            "key2" to TestEvent("message2")
        )

        val mockResult = mockk<SendResult<String, Any>>()
        val mockRecordMetadata = mockk<org.apache.kafka.clients.producer.RecordMetadata>()
        every { mockRecordMetadata.topic() } returns "test-topic"
        every { mockRecordMetadata.partition() } returns 0
        every { mockRecordMetadata.offset() } returns 0L
        every { mockResult.recordMetadata } returns mockRecordMetadata
        every { mockTemplate.send("test-topic", "key1", any()) } returns CompletableFuture.completedFuture(mockResult)
        every { mockTemplate.send("test-topic", "key2", any()) } returns CompletableFuture.completedFuture(mockResult)

        val result = publisher.publishEvents("test-topic", events)

        assert(result.isSuccess) { "Expected successful batch result" }
        assert(result.getOrNull()?.size == 2) { "Expected 2 successful operations" }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key1", any()) }
        verify(exactly = 1) { mockTemplate.send("test-topic", "key2", any()) }
    }

    data class TestEvent(val message: String)
}
