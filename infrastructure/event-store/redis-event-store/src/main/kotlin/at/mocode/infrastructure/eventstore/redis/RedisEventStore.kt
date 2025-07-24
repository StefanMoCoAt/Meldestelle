package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import at.mocode.infrastructure.eventstore.api.Subscription
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.Record
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.Subscription as RedisSubscription
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Redis Streams implementation of EventStore.
 */
class RedisEventStore(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) : EventStore {
    private val logger = LoggerFactory.getLogger(RedisEventStore::class.java)

    // Cache of stream versions to avoid reading from Redis for every append
    private val streamVersionCache = ConcurrentHashMap<UUID, Long>()

    // Active subscriptions
    private val subscriptions = ConcurrentHashMap<UUID, RedisSubscription>()

    // Listener containers for subscriptions
    private val listenerContainers = ConcurrentHashMap<UUID, StreamMessageListenerContainer<String, MapRecord<String, String, String>>>()

    override fun appendToStream(event: DomainEvent, streamId: UUID, expectedVersion: Long): Long {
        return appendToStream(listOf(event), streamId, expectedVersion)
    }

    override fun appendToStream(events: List<DomainEvent>, streamId: UUID, expectedVersion: Long): Long {
        if (events.isEmpty()) {
            return expectedVersion
        }

        // Check if all events belong to the same aggregate
        val aggregateId = events.first().aggregateId
        if (events.any { it.aggregateId != aggregateId }) {
            throw IllegalArgumentException("All events must belong to the same aggregate")
        }

        // Check if the stream ID matches the aggregate ID
        if (streamId != aggregateId) {
            throw IllegalArgumentException("Stream ID must match aggregate ID")
        }

        // Get the current version of the stream
        val currentVersion = getStreamVersion(streamId)

        // Check for concurrency conflicts
        if (expectedVersion != currentVersion) {
            throw ConcurrencyException(
                "Concurrency conflict: expected version $expectedVersion but got $currentVersion"
            )
        }

        // Append events to the stream
        var newVersion = currentVersion
        val streamKey = getStreamKey(streamId)

        for (event in events) {
            newVersion++

            // Ensure the event has the correct version
            if (event.version != newVersion) {
                throw IllegalArgumentException(
                    "Event version ${event.version} does not match expected version $newVersion"
                )
            }

            // Serialize the event
            val eventData = serializer.serialize(event)

            // Append to the stream
            val result = redisTemplate.opsForStream<String, String>()
                .add(streamKey, eventData)

            logger.debug("Appended event ${event.eventId} to stream $streamId with ID $result")

            // Also append to the all events stream
            val allEventsStreamKey = getAllEventsStreamKey()
            redisTemplate.opsForStream<String, String>()
                .add(allEventsStreamKey, eventData)
        }

        // Update the version cache
        streamVersionCache[streamId] = newVersion

        return newVersion
    }

    override fun readFromStream(streamId: UUID, fromVersion: Long, toVersion: Long?): List<DomainEvent> {
        val streamKey = getStreamKey(streamId)

        // Check if the stream exists
        if (!redisTemplate.hasKey(streamKey)) {
            return emptyList()
        }

        // Calculate the range of events to read
        val startOffset = if (fromVersion <= 0) ReadOffset.from("0") else ReadOffset.from("$fromVersion")
        val endOffset = toVersion?.let { "=$it" } ?: "+"

        // Read events from the stream
        val options = StreamReadOptions.empty()
            .count(toVersion?.let { (it - fromVersion + 1).toLong() } ?: Long.MAX_VALUE)

        val records = redisTemplate.opsForStream<String, String>()
            .read(options, StreamOffset.create(streamKey, startOffset))

        // Deserialize events
        return records?.mapNotNull { record ->
            try {
                val data = record.value
                serializer.deserialize(data)
            } catch (e: Exception) {
                logger.error("Error deserializing event from stream $streamId: ${e.message}", e)
                null
            }
        } ?: emptyList()
    }

    override fun readAllEvents(fromPosition: Long, maxCount: Int?): List<DomainEvent> {
        val streamKey = getAllEventsStreamKey()

        // Check if the stream exists
        if (!redisTemplate.hasKey(streamKey)) {
            return emptyList()
        }

        // Calculate the range of events to read
        val startOffset = if (fromPosition <= 0) ReadOffset.from("0") else ReadOffset.from("$fromPosition")

        // Read events from the stream
        val options = StreamReadOptions.empty()
            .count(maxCount?.toLong() ?: Long.MAX_VALUE)

        val records = redisTemplate.opsForStream<String, String>()
            .read(options, StreamOffset.create(streamKey, startOffset))

        // Deserialize events
        return records?.mapNotNull { record ->
            try {
                val data = record.value
                serializer.deserialize(data)
            } catch (e: Exception) {
                logger.error("Error deserializing event from all events stream: ${e.message}", e)
                null
            }
        } ?: emptyList()
    }

    override fun getStreamVersion(streamId: UUID): Long {
        // Check the cache first
        val cachedVersion = streamVersionCache[streamId]
        if (cachedVersion != null) {
            return cachedVersion
        }

        val streamKey = getStreamKey(streamId)

        // Check if the stream exists
        if (!redisTemplate.hasKey(streamKey)) {
            return -1
        }

        // Read all events from the stream to find the last real event (not init messages)
        val options = StreamReadOptions.empty()
        val records = redisTemplate.opsForStream<String, String>()
            .read(options, StreamOffset.create(streamKey, ReadOffset.from("0")))

        if (records == null || records.isEmpty()) {
            return -1
        }

        // Find the last real event (skip init messages)
        var lastVersion = -1L
        for (record in records.reversed()) {
            val data = record.value
            // Skip init messages (they only contain "init" -> "init")
            if (data.size == 1 && data.containsKey("init") && data["init"] == "init") {
                continue
            }

            try {
                val version = serializer.getVersion(data)
                lastVersion = version
                break
            } catch (e: Exception) {
                // Skip records that can't be deserialized as events
                logger.debug("Skipping record that can't be deserialized: ${e.message}")
                continue
            }
        }

        // Update the cache
        streamVersionCache[streamId] = lastVersion

        return lastVersion
    }

    override fun subscribeToStream(
        streamId: UUID,
        fromVersion: Long,
        handler: (DomainEvent) -> Unit
    ): Subscription {
        val streamKey = getStreamKey(streamId)

        // Create a unique ID for this subscription
        val subscriptionId = UUID.randomUUID()

        // Create a listener for the stream
        val listener = StreamListener<String, MapRecord<String, String, String>> { record ->
            try {
                val data = record.value
                val event = serializer.deserialize(data)
                handler(event)
            } catch (e: Exception) {
                logger.error("Error handling event from stream $streamId: ${e.message}", e)
            }
        }

        // Create a listener container
        val container = StreamMessageListenerContainer
            .create(redisTemplate.connectionFactory!!)

        // Start from the specified version or from the beginning if not specified
        val readOffset = if (fromVersion <= 0) ReadOffset.from("0") else ReadOffset.from("$fromVersion")

        // Create a subscription
        val subscription = container.receive(
            StreamOffset.create(streamKey, readOffset),
            listener
        )

        // Start the container
        container.start()

        // Store the subscription and container
        subscriptions[subscriptionId] = subscription
        listenerContainers[subscriptionId] = container

        // Return a subscription object
        return object : Subscription {
            private val active = AtomicBoolean(true)

            override fun unsubscribe() {
                if (active.compareAndSet(true, false)) {
                    subscription.cancel()
                    container.stop()
                    subscriptions.remove(subscriptionId)
                    listenerContainers.remove(subscriptionId)
                }
            }

            override fun isActive(): Boolean {
                return active.get()
            }
        }
    }

    override fun subscribeToAll(fromPosition: Long, handler: (DomainEvent) -> Unit): Subscription {
        val streamKey = getAllEventsStreamKey()

        // Create a unique ID for this subscription
        val subscriptionId = UUID.randomUUID()

        // Create a listener for the stream
        val listener = StreamListener<String, MapRecord<String, String, String>> { record ->
            try {
                val data = record.value
                val event = serializer.deserialize(data)
                handler(event)
            } catch (e: Exception) {
                logger.error("Error handling event from all events stream: ${e.message}", e)
            }
        }

        // Create a listener container
        val container = StreamMessageListenerContainer
            .create(redisTemplate.connectionFactory!!)

        // Start from the specified position or from the beginning if not specified
        val readOffset = if (fromPosition <= 0) ReadOffset.from("0") else ReadOffset.from("$fromPosition")

        // Create a subscription
        val subscription = container.receive(
            StreamOffset.create(streamKey, readOffset),
            listener
        )

        // Start the container
        container.start()

        // Store the subscription and container
        subscriptions[subscriptionId] = subscription
        listenerContainers[subscriptionId] = container

        // Return a subscription object
        return object : Subscription {
            private val active = AtomicBoolean(true)

            override fun unsubscribe() {
                if (active.compareAndSet(true, false)) {
                    subscription.cancel()
                    container.stop()
                    subscriptions.remove(subscriptionId)
                    listenerContainers.remove(subscriptionId)
                }
            }

            override fun isActive(): Boolean {
                return active.get()
            }
        }
    }

    /**
     * Gets the Redis key for a stream.
     *
     * @param streamId The ID of the stream
     * @return The Redis key for the stream
     */
    private fun getStreamKey(streamId: UUID): String {
        return "${properties.streamPrefix}$streamId"
    }

    /**
     * Gets the Redis key for the all events stream.
     *
     * @return The Redis key for the all events stream
     */
    private fun getAllEventsStreamKey(): String {
        return "${properties.streamPrefix}${properties.allEventsStream}"
    }
}
