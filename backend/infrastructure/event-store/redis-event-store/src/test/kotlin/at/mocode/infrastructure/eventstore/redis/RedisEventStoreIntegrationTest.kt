@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.core.domain.model.*
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
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
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

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
            registerEventType(TestCreatedEvent::class.java, "TestCreated")
            registerEventType(TestUpdatedEvent::class.java, "TestUpdated")
        }

        properties = RedisEventStoreProperties().apply {
            streamPrefix = "test-stream:"
            allEventsStream = "all-events"
            consumerGroup = "test-group"
            consumerName = "test-consumer"
        }

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
        val allEventsStreamKey = "${properties.streamPrefix}${properties.allEventsStream}"
        redisTemplate.delete(allEventsStreamKey)
    }

    @Test
    fun `event publishing and consuming with consumer groups should work`() {
        val aggregateId = Uuid.random()
        val event1 = TestCreatedEvent(aggregateId = AggregateId(aggregateId), version = EventVersion(1L), name = "Test Entity")
        val event2 = TestUpdatedEvent(aggregateId = AggregateId(aggregateId), version = EventVersion(2L), name = "Updated Test Entity")

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

        // KORREKTUR: Manuelles Auslösen des Pollings, da @Scheduled im Test nicht aktiv ist.
        eventConsumer.pollEvents()

        // Der Latch sollte jetzt fast sofort herunterzählen. Wir warten zur Sicherheit eine kurze Zeit.
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for events. Latch count: ${latch.count}")

        assertEquals(2, receivedEvents.size)

        val receivedEvent1 = receivedEvents.find { it.version == EventVersion(1L) } as TestCreatedEvent
        assertEquals(AggregateId(aggregateId), receivedEvent1.aggregateId)
        assertEquals("Test Entity", receivedEvent1.name)

        val receivedEvent2 = receivedEvents.find { it.version == EventVersion(2L) } as TestUpdatedEvent
        assertEquals(AggregateId(aggregateId), receivedEvent2.aggregateId)
        assertEquals("Updated Test Entity", receivedEvent2.name)
    }

    data class TestCreatedEvent(
        override val aggregateId: AggregateId,
        override val version: EventVersion,
        val name: String,
        override val eventType: EventType = EventType("TestCreated"),
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    data class TestUpdatedEvent(
        override val aggregateId: AggregateId,
        override val version: EventVersion,
        val name: String,
        override val eventType: EventType = EventType("TestUpdated"),
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)
}
