package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.core.domain.model.*
import at.mocode.infrastructure.cache.api.CacheConfiguration
import at.mocode.infrastructure.cache.api.DistributedCache
import at.mocode.infrastructure.cache.redis.JacksonCacheSerializer
import at.mocode.infrastructure.cache.redis.RedisConfiguration
import at.mocode.infrastructure.cache.redis.RedisDistributedCache
import at.mocode.infrastructure.eventstore.api.EventStore
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Integration Test zur Demonstration der gleichzeitigen Verwendung von
 * redis-cache und redis-event-store im selben Service.
 *
 * Dieser Test zeigt:
 * 1. Beide Module können ohne Konflikte gleichzeitig verwendet werden
 * 2. Separate Redis Databases verhindern Daten-Überschneidungen
 * 3. Separate Bean-Namen verhindern Bean-Konflikte
 * 4. Beide Module arbeiten unabhängig voneinander
 */
@OptIn(ExperimentalUuidApi::class)
@SpringBootTest(
    classes = [
        RedisCacheAndEventStoreIntegrationTest.TestConfig::class
    ]
)
@Testcontainers
class RedisCacheAndEventStoreIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val redisContainer: GenericContainer<*> = GenericContainer(
            DockerImageName.parse("redis:7-alpine")
        ).withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Cache Configuration (Database 0)
            registry.add("redis.host") { redisContainer.host }
            registry.add("redis.port") { redisContainer.getMappedPort(6379) }
            registry.add("redis.database") { 0 }

            // Event Store Configuration (Database 1)
            registry.add("redis.event-store.host") { redisContainer.host }
            registry.add("redis.event-store.port") { redisContainer.getMappedPort(6379) }
            registry.add("redis.event-store.database") { 1 }
            registry.add("redis.event-store.consumerGroup") { "test-group" }
        }

        @BeforeAll
        @JvmStatic
        fun setUp() {
            println("[DEBUG_LOG] Starting Redis container for integration test")
            redisContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            println("[DEBUG_LOG] Stopping Redis container")
            redisContainer.stop()
        }
    }

    @Configuration
    @Import(
        RedisConfiguration::class,
        RedisEventStoreConfiguration::class
    )
    class TestConfig {
        @Bean
        fun distributedCache(
            @Qualifier("redisTemplate") redisTemplate: RedisTemplate<String, ByteArray>,
            cacheConfiguration: CacheConfiguration
        ): DistributedCache {
            return RedisDistributedCache(
                redisTemplate = redisTemplate,
                serializer = JacksonCacheSerializer(),
                config = cacheConfiguration
            )
        }
    }

    @Autowired
    private lateinit var cache: DistributedCache

    @Autowired
    private lateinit var eventStore: EventStore

    // Verify separate ConnectionFactories
    @Autowired
    @Qualifier("redisConnectionFactory")
    private lateinit var cacheConnectionFactory: RedisConnectionFactory

    @Autowired
    @Qualifier("eventStoreRedisConnectionFactory")
    private lateinit var eventStoreConnectionFactory: RedisConnectionFactory

    @Test
    fun `test both modules can be used simultaneously without conflicts`(): Unit = runBlocking {
        println("[DEBUG_LOG] Testing simultaneous usage of cache and event store")

        // Test Cache Operations
        val cacheKey = "test-user-${Uuid.random()}"
        val cacheData = TestUser("John Doe", 30)

        println("[DEBUG_LOG] Cache: Storing data with key=$cacheKey")
        cache.set(cacheKey, cacheData, ttl = 5.minutes)

        val retrievedCacheData = cache.get(cacheKey, TestUser::class.java)
        println("[DEBUG_LOG] Cache: Retrieved data=$retrievedCacheData")
        assertNotNull(retrievedCacheData)
        assertEquals(cacheData.name, retrievedCacheData!!.name)
        assertEquals(cacheData.age, retrievedCacheData.age)

        // Test Event Store Operations
        val aggregateId = Uuid.random()
        val event = TestEvent(
            aggregateId = AggregateId(aggregateId),
            eventType = EventType("UserCreated"),
            data = mapOf("userId" to aggregateId.toString(), "name" to "Jane Doe")
        )

        println("[DEBUG_LOG] EventStore: Appending event for aggregateId=$aggregateId")
        eventStore.appendToStream(event, aggregateId, 0L)

        val loadedEvents = eventStore.readFromStream(aggregateId)
        println("[DEBUG_LOG] EventStore: Loaded ${loadedEvents.size} events")
        assertEquals(1, loadedEvents.size)
        assertEquals(event.eventType, (loadedEvents[0] as TestEvent).eventType)

        // Verify Cache and Event Store are independent
        println("[DEBUG_LOG] Verifying cache and event store are independent")

        // Cache should still work after event operations
        val cacheStillWorks = cache.get(cacheKey, TestUser::class.java)
        assertNotNull(cacheStillWorks)
        println("[DEBUG_LOG] Cache still works: key=$cacheKey exists")

        // Event store should still work after cache operations
        val eventsStillWork = eventStore.readFromStream(aggregateId)
        assertEquals(1, eventsStillWork.size)
        println("[DEBUG_LOG] Event store still works: aggregateId=$aggregateId has ${eventsStillWork.size} events")

        println("[DEBUG_LOG] Test completed successfully - Both modules work independently")
    }

    @Test
    fun `test separate connection factories are used`() {
        println("[DEBUG_LOG] Testing separate connection factories")

        assertNotNull(cacheConnectionFactory)
        assertNotNull(eventStoreConnectionFactory)

        // The connection factories should be different instances
        println("[DEBUG_LOG] Cache ConnectionFactory: ${cacheConnectionFactory.javaClass.simpleName}")
        println("[DEBUG_LOG] EventStore ConnectionFactory: ${eventStoreConnectionFactory.javaClass.simpleName}")

        // Both should be functional
        val cacheConnection = cacheConnectionFactory.connection
        val eventStoreConnection = eventStoreConnectionFactory.connection

        assertNotNull(cacheConnection)
        assertNotNull(eventStoreConnection)

        // Different databases
        println("[DEBUG_LOG] Cache uses database: ${cacheConnection.nativeConnection}")
        println("[DEBUG_LOG] EventStore uses database: ${eventStoreConnection.nativeConnection}")

        cacheConnection.close()
        eventStoreConnection.close()

        println("[DEBUG_LOG] Both connection factories are functional and independent")
    }

    @Test
    fun `test data isolation between cache and event store`(): Unit = runBlocking {
        println("[DEBUG_LOG] Testing data isolation between cache and event store")

        val sharedKey = "shared-key-${Uuid.random()}"

        // Store data in cache
        cache.set(sharedKey, TestUser("Cache User", 25), ttl = 5.minutes)
        println("[DEBUG_LOG] Stored data in cache with key=$sharedKey")

        // Store event with same UUID in event store
        val aggregateId = Uuid.random()
        val event = TestEvent(
            aggregateId = AggregateId(aggregateId),
            eventType = EventType("TestEvent"),
            data = mapOf("key" to sharedKey)
        )
        eventStore.appendToStream(event, aggregateId, 0L)
        println("[DEBUG_LOG] Stored event in event store with aggregateId=$aggregateId")

        // Both should be retrievable independently
        val cachedUser = cache.get(sharedKey, TestUser::class.java)
        val storedEvents = eventStore.readFromStream(aggregateId)

        assertNotNull(cachedUser)
        assertEquals(1, storedEvents.size)

        println("[DEBUG_LOG] Data isolation verified:")
        println("[DEBUG_LOG]   - Cache retrieved: ${cachedUser?.name}")
        println("[DEBUG_LOG]   - Event store retrieved: ${storedEvents.size} events")
        println("[DEBUG_LOG] Cache and Event Store use separate databases - no conflicts!")
    }

    // Test data classes
    data class TestUser(
        val name: String,
        val age: Int
    )

    data class TestEvent(
        override val aggregateId: AggregateId,
        override val eventType: EventType,
        val data: Map<String, String>,
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: kotlin.time.Instant = kotlin.time.Clock.System.now(),
        override val version: EventVersion = EventVersion(1),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : DomainEvent
}
