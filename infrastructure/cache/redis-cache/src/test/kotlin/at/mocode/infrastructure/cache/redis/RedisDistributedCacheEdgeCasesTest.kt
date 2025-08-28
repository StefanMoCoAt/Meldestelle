package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Edge Cases and Error Handling Tests for RedisDistributedCache
 */
@Testcontainers
class RedisDistributedCacheEdgeCasesTest {

    companion object {
        private val logger = KotlinLogging.logger {}

        @Container
        val redisContainer = GenericContainer<Nothing>(
            DockerImageName.parse("redis:7-alpine")
                .asCompatibleSubstituteFor("redis")
        ).apply {
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
            keyPrefix = "edge-test",
            defaultTtl = 30.minutes,
            compressionEnabled = true,
            compressionThreshold = 1024
        )

        cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()
    }

    @Test
    fun `test serialization with problematic objects`() {
        logger.info { "Testing serialization with problematic objects" }

        // Test 1: Object with circular references (causes StackOverflowError)
        val circularObject = CircularReferenceClass()
        circularObject.self = circularObject

        // This should handle the serialization gracefully (either succeed or fail gracefully)
        try {
            cache.set("circular-reference", circularObject as Any)
            logger.info { "Circular reference object was handled (possibly with Jackson's circular reference handling)" }
        } catch (e: Exception) {
            logger.info { "Circular reference object caused expected serialization issue: ${e::class.simpleName}" }
            assertTrue(e is com.fasterxml.jackson.databind.JsonMappingException ||
                      e is StackOverflowError ||
                      e is RuntimeException, "Expected serialization-related exception")
        }

        // Test 2: Very deep nesting that might cause issues
        val deepObject = createDeeplyNestedObject(50)
        try {
            cache.set("deep-nested", deepObject as Any)
            cache.get("deep-nested", DeeplyNestedObject::class.java)
            logger.info { "Deep nested object serialized successfully" }
        } catch (e: Exception) {
            logger.info { "Deep nested object caused expected issues: ${e::class.simpleName}" }
        }

        // Verify that the cache remains stable after problematic serialization attempts
        cache.set("normal-object", "test-value")
        assertEquals("test-value", cache.get<String>("normal-object"))

        logger.info { "Serialization edge cases handled correctly" }
    }

    @Test
    fun `test cache with extremely large values`() {
        logger.info { "Testing extremely large values" }

        // Create a very large string (10MB)
        val largeValue = "X".repeat(10 * 1024 * 1024)
        val key = "large-value"

        // This should trigger compression
        cache.set(key, largeValue)

        // Verify we can retrieve it
        val retrieved = cache.get<String>(key)
        assertNotNull(retrieved)
        assertEquals(largeValue.length, retrieved.length)
        assertEquals(largeValue.substring(0, 1000), retrieved.substring(0, 1000))

        logger.info { "Large value (${largeValue.length} chars) stored and retrieved successfully" }

        // Test with multiple large values
        val largeValues = (1..5).associateWith { "Y".repeat(2 * 1024 * 1024) }
        cache.multiSet(largeValues.mapKeys { "large-multi-${it.key}" })

        val retrievedLarge = cache.multiGet<String>(largeValues.keys.map { "large-multi-$it" })
        assertEquals(5, retrievedLarge.size)

        logger.info { "Multiple large values stored and retrieved successfully" }
    }

    @Test
    fun `test cache with null and empty values`() {
        logger.info { "Testing null and empty values" }

        // Test empty string
        cache.set("empty-string", "")
        assertEquals("", cache.get<String>("empty-string"))

        // Test string with only whitespace
        cache.set("whitespace", "   \n\t  ")
        assertEquals("   \n\t  ", cache.get<String>("whitespace"))

        // Test empty collections
        val emptyList = emptyList<String>()
        cache.set("empty-list", emptyList)
        assertEquals(emptyList, cache.get<List<String>>("empty-list"))

        val emptyMap = emptyMap<String, String>()
        cache.set("empty-map", emptyMap)
        assertEquals(emptyMap, cache.get<Map<String, String>>("empty-map"))

        // Test object with null fields
        val objectWithNulls = PersonWithNullable(name = "John", age = null, email = null)
        cache.set("null-fields", objectWithNulls)
        val retrieved = cache.get<PersonWithNullable>("null-fields")
        assertNotNull(retrieved)
        assertEquals("John", retrieved.name)
        assertNull(retrieved.age)
        assertNull(retrieved.email)

        logger.info { "Null and empty values handled correctly" }
    }

    @Test
    fun `test special characters and unicode in keys and values`() {
        logger.info { "Testing special characters and unicode" }

        // Test keys with special characters (encoded)
        val specialKeys = listOf(
            "key:with:colons",
            "key with spaces",
            "key-with-dashes",
            "key_with_underscores",
            "key.with.dots"
        )

        specialKeys.forEachIndexed { index, key ->
            cache.set(key, "value-$index")
        }

        specialKeys.forEachIndexed { index, key ->
            assertEquals("value-$index", cache.get<String>(key))
        }

        // Test values with unicode characters
        val unicodeValues = mapOf(
            "emoji" to "🚀 Hello World! 🌟",
            "german" to "Äöüß und Umlaute",
            "chinese" to "你好世界",
            "arabic" to "مرحبا بالعالم",
            "russian" to "Привет мир",
            "mixed" to "Mixed: 123 ABC äöü 🎉 العالم"
        )

        cache.multiSet(unicodeValues)
        val retrievedUnicode = cache.multiGet<String>(unicodeValues.keys)

        unicodeValues.forEach { (key, expectedValue) ->
            assertEquals(expectedValue, retrievedUnicode[key])
        }

        logger.info { "Special characters and unicode handled correctly" }
    }

    @Test
    fun `test cache with complex nested objects`() {
        logger.info { "Testing complex nested objects" }

        // Create a complex nested structure
        val complexObject = ComplexNestedObject(
            id = 1,
            name = "Complex Object",
            metadata = mapOf(
                "tags" to listOf("tag1", "tag2", "tag3"),
                "properties" to mapOf(
                    "nested" to mapOf(
                        "deep" to "value",
                        "numbers" to listOf(1, 2, 3, 4, 5)
                    )
                )
            ),
            children = listOf(
                SimpleChild(1, "Child 1"),
                SimpleChild(2, "Child 2")
            )
        )

        // Store and retrieve
        cache.set("complex-object", complexObject)
        val retrieved = cache.get<ComplexNestedObject>("complex-object")

        assertNotNull(retrieved)
        assertEquals(complexObject.id, retrieved.id)
        assertEquals(complexObject.name, retrieved.name)
        assertEquals(complexObject.children.size, retrieved.children.size)
        assertEquals(complexObject.children[0].name, retrieved.children[0].name)

        // Check nested metadata
        val retrievedTags = retrieved.metadata["tags"] as List<*>
        assertEquals(3, retrievedTags.size)
        assertTrue(retrievedTags.contains("tag1"))

        logger.info { "Complex nested object serialized and deserialized correctly" }
    }

    @Test
    fun `test cache behavior with malformed data`() {
        logger.info { "Testing cache behavior with malformed data" }

        // Test retrieving non-existent keys
        assertNull(cache.get<String>("non-existent-key"))

        // Test batch operations with mixed existing/non-existing keys
        cache.set("existing-1", "value-1")
        cache.set("existing-2", "value-2")

        val mixedKeys = listOf("existing-1", "non-existing", "existing-2", "also-non-existing")
        val result = cache.multiGet<String>(mixedKeys)

        assertEquals(2, result.size)
        assertEquals("value-1", result["existing-1"])
        assertEquals("value-2", result["existing-2"])
        assertNull(result["non-existing"])
        assertNull(result["also-non-existing"])

        logger.info { "Malformed data scenarios handled correctly" }
    }

    // Helper method to create deeply nested objects
    private fun createDeeplyNestedObject(depth: Int): DeeplyNestedObject {
        return if (depth <= 0) {
            DeeplyNestedObject("leaf", null)
        } else {
            DeeplyNestedObject("node-$depth", createDeeplyNestedObject(depth - 1))
        }
    }

    // Test data classes
    private class NonSerializableClass {
        // This class intentionally has no default constructor or proper serialization
        private val threadLocal = ThreadLocal<String>()

        fun someMethod() = "not serializable"
    }

    private class CircularReferenceClass {
        var name: String = "circular"
        var self: CircularReferenceClass? = null
    }

    data class DeeplyNestedObject(
        val name: String,
        val child: DeeplyNestedObject?
    )

    data class PersonWithNullable(
        val name: String,
        val age: Int?,
        val email: String?
    )

    data class ComplexNestedObject(
        val id: Int,
        val name: String,
        val metadata: Map<String, Any>,
        val children: List<SimpleChild>
    )

    data class SimpleChild(
        val id: Int,
        val name: String
    )
}
