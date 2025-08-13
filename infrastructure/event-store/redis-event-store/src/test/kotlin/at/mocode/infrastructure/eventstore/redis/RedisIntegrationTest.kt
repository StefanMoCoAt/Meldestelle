package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.core.domain.model.AggregateId
import at.mocode.core.domain.model.EventType
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
class RedisIntegrationTest {

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
        properties = RedisEventStoreProperties(
            streamPrefix = "test-stream:",
            allEventsStream = "all-events",
            consumerGroup = "test-group",
            consumerName = "test-consumer"
        )
        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        eventConsumer = RedisEventConsumer(redisTemplate, serializer, properties)
        cleanupRedis()
        eventConsumer.init()
    }


    @AfterEach
    fun tearDown() {
        eventConsumer.shutdown()
        cleanupRedis()
    }

    private fun cleanupRedis() {
        val allEventsStreamKey = "${properties.streamPrefix}${properties.allEventsStream}"
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
        redisTemplate.delete(allEventsStreamKey)
    }

    @Test
    fun `event publishing and consuming should be fast and reliable`() {
        val aggregateId = uuid4()
        val event1 = TestCreatedEvent(AggregateId(aggregateId), EventVersion(1L), "Test Entity")
        val event2 = TestUpdatedEvent(AggregateId(aggregateId), EventVersion(2L), "Updated Test Entity")

        val receivedEvents = mutableListOf<DomainEvent>()
        eventConsumer.registerEventHandler("TestCreated") { receivedEvents.add(it) }
        eventConsumer.registerEventHandler("TestUpdated") { receivedEvents.add(it) }

        eventStore.appendToStream(listOf(event1, event2), aggregateId, 0)

        eventConsumer.pollEvents()

        assertEquals(2, receivedEvents.size)

        val receivedEvent1 = receivedEvents.find { it.version == EventVersion(1L) } as TestCreatedEvent
        assertEquals(AggregateId(aggregateId), receivedEvent1.aggregateId)
        assertEquals("Test Entity", receivedEvent1.name)

        val receivedEvent2 = receivedEvents.find { it.version == EventVersion(2L) } as TestUpdatedEvent
        assertEquals(AggregateId(aggregateId), receivedEvent2.aggregateId)
        assertEquals("Updated Test Entity", receivedEvent2.name)
    }

    @Serializable
    data class TestCreatedEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(uuid4()),
        @Transient override val version: EventVersion = EventVersion(0),
        val name: String
    ) : BaseDomainEvent(aggregateId, EventType("TestCreated"), version)

    @Serializable
    data class TestUpdatedEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(uuid4()),
        @Transient override val version: EventVersion = EventVersion(0),
        val name: String
    ) : BaseDomainEvent(aggregateId, EventType("TestUpdated"), version)
}
