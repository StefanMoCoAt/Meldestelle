package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Testcontainers
class RedisEventStoreIntegrationTest {

    companion object {
        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var serializer: EventSerializer
    private lateinit var properties: RedisEventStoreProperties
    private lateinit var eventStore: EventStore
    private lateinit var eventConsumer: RedisEventConsumer

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = StringRedisTemplate(connectionFactory)

        serializer = JacksonEventSerializer().apply {
            registerEventType("TestCreated" as Class<out DomainEvent>, TestCreatedEvent::class.java as String)
            registerEventType("TestUpdated" as Class<out DomainEvent>, TestUpdatedEvent::class.java as String)
        }

        properties = RedisEventStoreProperties(
            streamPrefix = "test-stream:",
            allEventsStream = "all-events",
            consumerGroup = "test-group",
            consumerName = "test-consumer"
        )

        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        eventConsumer = RedisEventConsumer(redisTemplate, serializer, properties)

        cleanupRedis()
    }

    @AfterEach
    fun tearDown() {
        eventConsumer.shutdown()
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
    fun `event publishing and consuming with consumer groups should work`() {
        val aggregateId = uuid4()
        val event1 = TestCreatedEvent(aggregateId = aggregateId, version = 1L, name = "Test Entity")
        val event2 = TestUpdatedEvent(aggregateId = aggregateId, version = 2L, name = "Updated Test Entity")

        val latch = CountDownLatch(2)
        val receivedEvents = mutableListOf<DomainEvent>()

        eventConsumer.registerEventHandler("TestCreated") { event ->
            receivedEvents.add(event)
            latch.countDown()
        }
        eventConsumer.registerEventHandler("TestUpdated") { event ->
            receivedEvents.add(event)
            latch.countDown()
        }

        eventConsumer.init()

        eventStore.appendToStream(listOf(event1, event2), aggregateId, 0)

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for events")

        assertEquals(2, receivedEvents.size)

        val receivedEvent1 = receivedEvents.find { it.version == 1L } as TestCreatedEvent
        assertEquals(aggregateId, receivedEvent1.aggregateId)
        assertEquals("Test Entity", receivedEvent1.name)

        val receivedEvent2 = receivedEvents.find { it.version == 2L } as TestUpdatedEvent
        assertEquals(aggregateId, receivedEvent2.aggregateId)
        assertEquals("Updated Test Entity", receivedEvent2.name)
    }

    // Hilfsklassen für Tests, die von BaseDomainEvent erben
    data class TestCreatedEvent(
        override val aggregateId: Uuid,
        override val version: Long,
        val name: String,
        override val eventType: String = "TestCreated",
        override val eventId: Uuid = uuid4(),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: Uuid? = null,
        override val causationId: Uuid? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    data class TestUpdatedEvent(
        override val aggregateId: Uuid,
        override val version: Long,
        val name: String,
        override val eventType: String = "TestUpdated",
        override val eventId: Uuid = uuid4(),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: Uuid? = null,
        override val causationId: Uuid? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)
}
