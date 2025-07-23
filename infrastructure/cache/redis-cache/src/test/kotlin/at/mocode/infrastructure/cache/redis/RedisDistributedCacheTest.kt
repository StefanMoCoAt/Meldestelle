package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.CacheConfiguration
import at.mocode.infrastructure.cache.api.CacheSerializer
import at.mocode.infrastructure.cache.api.ConnectionState
import at.mocode.infrastructure.cache.api.DefaultCacheConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Testcontainers
class RedisDistributedCacheTest {

    companion object {
        @Container
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
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
            offlineModeEnabled = true
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
        cache.synchronize()

        // Verify connection state is CONNECTED
        assertEquals(ConnectionState.CONNECTED, cache.getConnectionState())

        // Verify the value set while offline is now in Redis
        assertEquals("value2", cache.get("offline2", String::class.java))

        // Verify it's no longer marked as dirty
        assertFalse(cache.getDirtyKeys().contains("offline2"))
    }

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

    // Test data class
    data class Person(
        val name: String,
        val age: Int,
        val hobbies: List<String>
    )
}
