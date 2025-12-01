package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.AggregateId
import at.mocode.core.domain.model.EventType
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.uuid.Uuid

@Testcontainers
class RedisEventStoreTest {

    companion object {
        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var serializer: EventSerializer
    private lateinit var properties: RedisEventStoreProperties
    private lateinit var eventStore: RedisEventStore

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = StringRedisTemplate(connectionFactory)

        serializer = JacksonEventSerializer().apply {
            registerEventType(TestCreatedEvent::class.java, "TestCreated")
            registerEventType(TestUpdatedEvent::class.java, "TestUpdated")
        }

        properties = RedisEventStoreProperties().apply {
            streamPrefix = "test-stream:"
        }
        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        cleanupRedis()
    }

    @AfterEach
    fun tearDown() = cleanupRedis()

    private fun cleanupRedis() {
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    @Test
    fun `append and read events should work correctly for new stream`() {
        val aggregateId = Uuid.random()
        val event1 = TestCreatedEvent(AggregateId(aggregateId), EventVersion(1L), "Test Entity")
        val event2 = TestUpdatedEvent(AggregateId(aggregateId), EventVersion(2L), "Updated Test Entity")

        eventStore.appendToStream(listOf(event1, event2), aggregateId, 0)

        val events = eventStore.readFromStream(aggregateId)
        assertEquals(2, events.size)

        val firstEvent = events[0] as TestCreatedEvent
        assertEquals(EventVersion(1L), firstEvent.version)
        assertEquals("Test Entity", firstEvent.name)

        val secondEvent = events[1] as TestUpdatedEvent
        assertEquals(EventVersion(2L), secondEvent.version)
        assertEquals("Updated Test Entity", secondEvent.name)
    }

    @Test
    fun `appending with wrong expected version should throw ConcurrencyException`() {
        val aggregateId = Uuid.random()
        val event1 = TestCreatedEvent(AggregateId(aggregateId), EventVersion(1L), "Test Entity")
        eventStore.appendToStream(listOf(event1), aggregateId, 0) // Stream is now at version 1

        val event2 = TestUpdatedEvent(AggregateId(aggregateId), EventVersion(2L), "Updated Test Entity")
        assertThrows<ConcurrencyException> {
            eventStore.appendToStream(listOf(event2), aggregateId, 0)
        }
    }

    @Serializable
    data class TestCreatedEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val name: String
    ) : BaseDomainEvent(aggregateId, EventType("TestCreated"), version)

    @Serializable
    data class TestUpdatedEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val name: String
    ) : BaseDomainEvent(aggregateId, EventType("TestUpdated"), version)
}
