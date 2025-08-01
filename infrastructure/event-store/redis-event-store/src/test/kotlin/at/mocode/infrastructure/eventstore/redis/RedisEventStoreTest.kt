package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Instant
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

        redisTemplate = StringRedisTemplate()
        redisTemplate.connectionFactory = connectionFactory
        redisTemplate.afterPropertiesSet()

        serializer = JacksonEventSerializer().apply {
            registerEventType("TestCreated" as Class<out DomainEvent>, TestCreatedEvent::class.java as String)
            registerEventType("TestUpdated" as Class<out DomainEvent>, TestUpdatedEvent::class.java as String)
        }

        properties = RedisEventStoreProperties(
            streamPrefix = "test-stream:",
            allEventsStream = "all-events"
        )

        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        cleanupRedis()
    }

    @AfterEach
    fun tearDown() {
        cleanupRedis()
    }

    private fun cleanupRedis() {
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
        redisTemplate.delete(properties.allEventsStream)
    }

    @Test
    fun `append and read events should work correctly`() {
        val aggregateId = uuid4()
        val event1 = TestCreatedEvent(aggregateId = aggregateId, version = 1L, name = "Test Entity")
        val event2 = TestUpdatedEvent(aggregateId = aggregateId, version = 2L, name = "Updated Test Entity")

        eventStore.appendToStream(listOf(event1, event2), aggregateId, 0)

        val events = eventStore.readFromStream(aggregateId)
        assertEquals(2, events.size)

        val firstEvent = events[0] as TestCreatedEvent
        assertEquals(aggregateId, firstEvent.aggregateId)
        assertEquals(1L, firstEvent.version)
        assertEquals("Test Entity", firstEvent.name)

        val secondEvent = events[1] as TestUpdatedEvent
        assertEquals(aggregateId, secondEvent.aggregateId)
        assertEquals(2L, secondEvent.version)
        assertEquals("Updated Test Entity", secondEvent.name)
    }

    @Test
    fun `appending with wrong expected version should throw ConcurrencyException`() {
        val aggregateId = uuid4()
        val event1 = TestCreatedEvent(aggregateId = aggregateId, version = 1L, name = "Test Entity")
        eventStore.appendToStream(listOf(event1), aggregateId, 0)

        val event2 = TestUpdatedEvent(aggregateId = aggregateId, version = 2L, name = "Updated Test Entity")
        assertThrows<ConcurrencyException> {
            eventStore.appendToStream(listOf(event2), aggregateId, 0) // Wrong version
        }
    }

    // Hilfsklassen für Tests, die von BaseDomainEvent erben
    data class TestCreatedEvent(
        override val aggregateId: Uuid,
        override val version: Long,
        val name: String,
        override val eventType: String = "TestCreated",
        override val eventId: Uuid = uuid4(),
        override val timestamp: kotlin.time.Instant = kotlin.time.Clock.System.now(),
        override val correlationId: Uuid? = null,
        override val causationId: Uuid? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    data class TestUpdatedEvent(
        override val aggregateId: Uuid,
        override val version: Long,
        val name: String,
        override val eventType: String = "TestUpdated",
        override val eventId: Uuid = uuid4(),
        override val timestamp: kotlin.time.Instant = kotlin.time.Clock.System.now(),
        override val correlationId: Uuid? = null,
        override val causationId: Uuid? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)
}
