package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration // Alias für Eindeutigkeit

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
            keyPrefix = "test",
            offlineModeEnabled = true,
            defaultTtl = 30.minutes
        )

        cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()
    }

    @AfterEach
    fun tearDown() {
        cache.clear()
    }

    @Test
    fun `get should return value with new reified extension function`() {
        cache.set("key1", "value1")
        val value = cache.get<String>("key1")
        assertEquals("value1", value)
    }

    @Test
    fun `test basic cache operations`() {
        cache.set("key1", "value1")
        val value = cache.get("key1", String::class.java)
        assertEquals("value1", value)
        assertTrue(cache.exists("key1"))
        cache.delete("key1")
        assertFalse(cache.exists("key1"))
        assertNull(cache.get("key1", String::class.java))
    }

    @Test
    fun `test cache with TTL`() {
        cache.set("key2", "value2", 100.milliseconds)
        assertTrue(cache.exists("key2"))
        Thread.sleep(200)
        assertFalse(cache.exists("key2"))
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
    fun `should handle offline mode and synchronize correctly`() {
        // Arrange
        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>(relaxed = true)
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>(relaxed = true)
        every { mockTemplate.opsForValue() } returns mockValueOps

        val offlineCache = RedisDistributedCache(mockTemplate, serializer, config)

        // 1. Online-Phase
        every { mockValueOps.set(any<String>(), any<ByteArray>(), any<JavaDuration>()) } returns Unit
        offlineCache.set("key1", "online-value")
        verify(exactly = 1) { mockValueOps.set(eq("test:key1"), any<ByteArray>(), any<JavaDuration>()) }

        // 2. Offline-Phase simulieren
        every {
            mockValueOps.set(
                any<String>(),
                any<ByteArray>(),
                any<JavaDuration>()
            )
        } throws RedisConnectionFailureException("Redis is down")
        every { mockTemplate.delete(any<String>()) } throws RedisConnectionFailureException("Redis is down")

        offlineCache.set("key2", "offline-value")
        offlineCache.delete("key1")

        assertEquals("offline-value", offlineCache.get<String>("key2"))
        assertTrue(offlineCache.getDirtyKeys().contains("key2"))
        assertTrue(offlineCache.getDirtyKeys().contains("key1"))

        // 3. Wiederverbindungs-Phase
        every { mockValueOps.set(any<String>(), any<ByteArray>(), any<JavaDuration>()) } returns Unit
        every { mockTemplate.delete(any<String>()) } returns true
        every { mockTemplate.hasKey("connection-test") } returns true

        offlineCache.checkConnection()

        verify(exactly = 1) { mockValueOps.set(eq("test:key1"), any<ByteArray>(), any<JavaDuration>()) }
        verify(exactly = 1) { mockTemplate.delete(eq("test:key1")) }
        assertTrue(offlineCache.getDirtyKeys().isEmpty(), "Dirty keys should be empty after sync")
    }

    @Test
    fun `test multiSet with TTL`() {
        val entries = mapOf("batchTtl1" to "value1", "batchTtl2" to "value2")
        cache.multiSet(entries, 100.milliseconds)

        assertTrue(cache.exists("batchTtl1"))
        Thread.sleep(200)
        assertFalse(cache.exists("batchTtl1"))
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

    // Test data class
    data class Person(
        val name: String,
        val age: Int,
        val hobbies: List<String>
    )
}
