package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.CacheConfiguration
import at.mocode.infrastructure.cache.api.CacheSerializer
import at.mocode.infrastructure.cache.api.ConnectionState
import at.mocode.infrastructure.cache.api.DefaultCacheConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.test.*

@Testcontainers
class RedisDistributedCacheTest {

    companion object {
        @Container
        val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine")).apply {
            withExposedPorts(6379)
        }
    }

    private lateinit var redisTemplate: RedisTemplate<String, ByteArray>
    private lateinit var serializer: CacheSerializer
    private lateinit var config: CacheConfiguration
    private lateinit var cache: RedisDistributedCache

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = RedisTemplate<String, ByteArray>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            afterPropertiesSet()
        }

        serializer = JacksonCacheSerializer()
        config = DefaultCacheConfiguration(
            keyPrefix = "test:",
            offlineModeEnabled = true,
            defaultTtl = Duration.ofMinutes(30)
        )

        cache = RedisDistributedCache(redisTemplate, serializer, config)

        // Clear the cache before each test
        cache.clear()
    }

    @AfterEach
    fun tearDown() {
        cache.clear()
    }

    @Test
    fun `test basic cache operations`() {
        // Set a value
        cache.set("key1", "value1")

        // Get the value
        val value = cache.get("key1", String::class.java)
        assertEquals("value1", value)

        // Check if the key exists
        assertTrue(cache.exists("key1"))

        // Delete the key
        cache.delete("key1")

        // Verify it's gone
        assertFalse(cache.exists("key1"))
        assertNull(cache.get("key1", String::class.java))
    }

    @Test
    fun `test cache with TTL`() {
        // Set a value with a short TTL
        cache.set("key2", "value2", Duration.ofMillis(100))

        // Verify it exists
        assertTrue(cache.exists("key2"))
        assertEquals("value2", cache.get("key2", String::class.java))

        // Wait for it to expire
        Thread.sleep(200)

        // Verify it's gone
        assertFalse(cache.exists("key2"))
        assertNull(cache.get("key2", String::class.java))
    }

    @Test
    fun `test batch operations`() {
        // Set multiple values
        val entries = mapOf(
            "batch1" to "value1",
            "batch2" to "value2",
            "batch3" to "value3"
        )
        cache.multiSet(entries)

        // Get multiple values
        val values = cache.multiGet(listOf("batch1", "batch2", "batch3"), String::class.java)
        assertEquals(3, values.size)
        assertEquals("value1", values["batch1"])
        assertEquals("value2", values["batch2"])
        assertEquals("value3", values["batch3"])

        // Delete multiple values
        cache.multiDelete(listOf("batch1", "batch3"))

        // Verify they're gone
        val remainingValues = cache.multiGet(listOf("batch1", "batch2", "batch3"), String::class.java)
        assertEquals(1, remainingValues.size)
        assertNull(remainingValues["batch1"])
        assertEquals("value2", remainingValues["batch2"])
        assertNull(remainingValues["batch3"])
    }

    // Note: Tests that stop and restart the container are commented out
    // as they interfere with the Testcontainers lifecycle management
    /*
    @Test
    fun `test offline capability`() {
        // Set a value
        cache.set("offline1", "value1")

        // Simulate going offline by stopping the Redis container
        redisContainer.stop()

        // Verify connection state is DISCONNECTED
        assertEquals(ConnectionState.DISCONNECTED, cache.getConnectionState())

        // We should still be able to get the value from local cache
        assertEquals("value1", cache.get("offline1", String::class.java))

        // Set a new value while offline
        cache.set("offline2", "value2")

        // Verify it's marked as dirty
        assertTrue(cache.getDirtyKeys().contains("offline2"))

        // Start Redis again
        redisContainer.start()

        // Manually trigger synchronization
        cache.synchronize(null)

        // Verify connection state is CONNECTED
        assertEquals(ConnectionState.CONNECTED, cache.getConnectionState())

        // Verify the value set while offline is now in Redis
        assertEquals("value2", cache.get("offline2", String::class.java))

        // Verify it's no longer marked as dirty
        assertFalse(cache.getDirtyKeys().contains("offline2"))
    }
    */

    @Test
    fun `test complex objects`() {
        // Create a complex object
        val person = Person("John Doe", 30, listOf("Reading", "Hiking"))

        // Store it in the cache
        cache.set("person1", person)

        // Retrieve it
        val retrievedPerson = cache.get("person1", Person::class.java)

        // Verify it's the same
        assertNotNull(retrievedPerson)
        assertEquals("John Doe", retrievedPerson.name)
        assertEquals(30, retrievedPerson.age)
        assertEquals(2, retrievedPerson.hobbies.size)
        assertTrue(retrievedPerson.hobbies.contains("Reading"))
        assertTrue(retrievedPerson.hobbies.contains("Hiking"))
    }

    // Note: Tests that stop and restart the container are commented out
    /*
    @Test
    fun `test connection state listeners`() {
        // Create a mock listener
        val listener = mockk<ConnectionStateListener>(relaxed = true)

        // Register the listener
        cache.registerConnectionListener(listener)

        // Simulate disconnection
        redisContainer.stop()

        // Manually trigger connection check
        cache.checkConnection()

        // Verify listener was called with DISCONNECTED state
        verify(exactly = 1) {
            listener.onConnectionStateChanged(ConnectionState.DISCONNECTED, any())
        }

        // Start Redis again
        redisContainer.start()

        // Manually trigger connection check
        cache.checkConnection()

        // Verify listener was called with CONNECTED state
        verify(exactly = 1) {
            listener.onConnectionStateChanged(ConnectionState.CONNECTED, any())
        }

        // Unregister the listener
        cache.unregisterConnectionListener(listener)

        // Simulate disconnection again
        redisContainer.stop()
        cache.checkConnection()

        // Verify listener was not called again (still only once for DISCONNECTED)
        verify(exactly = 1) {
            listener.onConnectionStateChanged(ConnectionState.DISCONNECTED, any())
        }
    }

    @Test
    fun `test scheduled tasks`() {
        // Set a value with a short TTL
        cache.set("scheduled1", "value1", Duration.ofMillis(100))

        // Wait for it to expire
        Thread.sleep(200)

        // Manually trigger cleanup
        cache.cleanupLocalCache()

        // Verify it's gone from local cache
        assertNull(cache.get("scheduled1", String::class.java))

        // Set a value while Redis is down
        redisContainer.stop()
        cache.set("scheduled2", "value2")

        // Verify it's marked as dirty
        assertTrue(cache.getDirtyKeys().contains("scheduled2"))

        // Start Redis again
        redisContainer.start()

        // Manually trigger scheduled sync
        cache.scheduledSync()

        // Verify it's no longer marked as dirty
        assertFalse(cache.getDirtyKeys().contains("scheduled2"))
    }

    @Test
    fun `test synchronize with specific keys`() {
        // Set multiple values
        cache.set("sync1", "value1")
        cache.set("sync2", "value2")
        cache.set("sync3", "value3")

        // Simulate going offline
        redisContainer.stop()

        // Update values while offline
        cache.set("sync1", "updated1")
        cache.set("sync2", "updated2")

        // Verify they're marked as dirty
        assertTrue(cache.getDirtyKeys().contains("sync1"))
        assertTrue(cache.getDirtyKeys().contains("sync2"))

        // Start Redis again
        redisContainer.start()

        // Synchronize only specific keys
        cache.synchronize(listOf("sync1"))

        // Verify only sync1 is no longer dirty
        assertFalse(cache.getDirtyKeys().contains("sync1"))
        assertTrue(cache.getDirtyKeys().contains("sync2"))

        // Verify the values in Redis
        assertEquals("updated1", cache.get("sync1", String::class.java))

        // Now synchronize all
        cache.synchronize(null)

        // Verify all are no longer dirty
        assertFalse(cache.getDirtyKeys().contains("sync2"))
    }
    */

    @Test
    fun `test clear method`() {
        // Set multiple values
        cache.set("clear1", "value1")
        cache.set("clear2", "value2")

        // Verify they exist
        assertTrue(cache.exists("clear1"))
        assertTrue(cache.exists("clear2"))

        // Clear the cache
        cache.clear()

        // Verify they're gone
        assertFalse(cache.exists("clear1"))
        assertFalse(cache.exists("clear2"))
    }

    @Test
    fun `test markDirty method`() {
        // Set a value
        cache.set("dirty1", "value1")

        // Mark it as dirty
        cache.markDirty("dirty1")

        // Verify it's in the dirty keys
        assertTrue(cache.getDirtyKeys().contains("dirty1"))
    }

    @Test
    fun `test handling Redis connection failures`() {
        // Create a mock RedisTemplate and ValueOperations
        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>()
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>()

        // Configure the mock to throw connection failure
        every { mockTemplate.opsForValue() } returns mockValueOps
        every { mockValueOps.get(any()) } throws RedisConnectionFailureException("Test connection failure")
        every { mockTemplate.hasKey(any()) } throws RedisConnectionFailureException("Test connection failure")

        // Create a cache with the mock
        val mockCache = RedisDistributedCache(mockTemplate, serializer, config)

        // Try to get a value
        val value = mockCache.get("failure1", String::class.java)

        // Verify it returns null
        assertNull(value)

        // Verify the connection state is DISCONNECTED
        assertEquals(ConnectionState.DISCONNECTED, mockCache.getConnectionState())
    }

    @Test
    fun `test default TTL`() {
        // Set a value without specifying TTL
        cache.set("defaultTtl", "value")

        // Verify it exists
        assertTrue(cache.exists("defaultTtl"))

        // The default TTL is 30 minutes, so it should still exist
        assertEquals("value", cache.get("defaultTtl", String::class.java))
    }

    @Test
    fun `test multiSet with TTL`() {
        // Set multiple values with TTL
        val entries = mapOf(
            "batchTtl1" to "value1",
            "batchTtl2" to "value2"
        )
        cache.multiSet(entries, Duration.ofMillis(100))

        // Verify they exist
        assertTrue(cache.exists("batchTtl1"))
        assertTrue(cache.exists("batchTtl2"))

        // Wait for them to expire
        Thread.sleep(200)

        // Verify they're gone
        assertFalse(cache.exists("batchTtl1"))
        assertFalse(cache.exists("batchTtl2"))
    }

    // Test data class
    data class Person(
        val name: String,
        val age: Int,
        val hobbies: List<String>
    )
}
