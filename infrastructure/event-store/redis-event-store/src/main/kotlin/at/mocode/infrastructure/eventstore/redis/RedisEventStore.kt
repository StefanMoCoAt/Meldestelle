package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import at.mocode.infrastructure.eventstore.api.Subscription
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RedisEventStore(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) : EventStore {
    private val logger = LoggerFactory.getLogger(RedisEventStore::class.java)
    private val streamVersionCache = ConcurrentHashMap<UUID, Long>()

    override fun appendToStream(events: List<DomainEvent>, streamId: UUID, expectedVersion: Long): Long {
        if (events.isEmpty()) {
            logger.debug("Empty event list provided for stream {}, returning current version", streamId)
            return getStreamVersion(streamId)
        }

        val aggregateId = events.first().aggregateId
        require(events.all { it.aggregateId == aggregateId }) {
            "All events must belong to the same aggregate. Expected: $aggregateId, but found mixed aggregate IDs"
        }
        require(streamId == aggregateId.value) {
            "Stream ID $streamId must match aggregate ID ${aggregateId.value}"
        }

        logger.debug("Appending {} events to stream {} with expected version {}", events.size, streamId, expectedVersion)
        var currentVersion = getStreamVersion(streamId)

        if (currentVersion != expectedVersion) {
            logger.warn("Version conflict detected for stream {}. Expected: {}, current: {}", streamId, expectedVersion, currentVersion)
            streamVersionCache.remove(streamId) // Invalidate cache on conflict
            val actualVersion = getStreamVersion(streamId) // Re-fetch from Redis
            if (actualVersion != expectedVersion) {
                throw ConcurrencyException("Concurrency conflict for stream $streamId: expected version $expectedVersion but got $actualVersion")
            }
            currentVersion = actualVersion
        }

        for (event in events) {
            currentVersion = appendToStreamInternal(event, streamId, currentVersion)
        }

        logger.info("Successfully appended {} events to stream {}. New version: {}", events.size, streamId, currentVersion)
        return currentVersion
    }

    override fun appendToStream(event: DomainEvent, streamId: UUID, expectedVersion: Long): Long {
        logger.debug("Appending single event to stream {} with expected version {}", streamId, expectedVersion)
        var currentVersion = getStreamVersion(streamId)

        if (currentVersion != expectedVersion) {
            logger.warn("Version conflict detected for stream {}. Expected: {}, current: {}", streamId, expectedVersion, currentVersion)
            streamVersionCache.remove(streamId) // Invalidate cache on conflict
            val actualVersion = getStreamVersion(streamId) // Re-fetch from Redis
            if (actualVersion != expectedVersion) {
                throw ConcurrencyException("Concurrency conflict for stream $streamId: expected version $expectedVersion but got $actualVersion")
            }
            currentVersion = actualVersion
        }

        val newVersion = appendToStreamInternal(event, streamId, currentVersion)
        logger.info("Successfully appended event to stream {}. New version: {}", streamId, newVersion)
        return newVersion
    }

    private fun appendToStreamInternal(event: DomainEvent, streamId: UUID, currentVersion: Long): Long {
        val newVersion = currentVersion + 1
        require(event.version.value == newVersion) {
            "Event version ${event.version.value} does not match expected new version $newVersion for stream $streamId"
        }

        val streamKey = getStreamKey(streamId)
        val allEventsStreamKey = getAllEventsStreamKey()
        val eventData = serializer.serialize(event)

        logger.debug("Writing event {} to stream {} and all-events stream atomically", event.eventId, streamId)

        try {
            redisTemplate.execute(object : SessionCallback<List<Any>> {
                @Throws(DataAccessException::class)
                override fun <K : Any?, V : Any?> execute(operations: org.springframework.data.redis.core.RedisOperations<K, V>): List<Any> {
                    val streamOps = (operations as StringRedisTemplate).opsForStream<String, String>()

                    operations.multi()
                    streamOps.add(streamKey, eventData)
                    streamOps.add(allEventsStreamKey, eventData)

                    return operations.exec()
                }
            })

            streamVersionCache[streamId] = newVersion
            logger.debug("Successfully wrote event {} to Redis streams, updated cache version to {}", event.eventId, newVersion)
            return newVersion
        } catch (e: Exception) {
            logger.error("Failed to append event {} transactionally for stream {}: {}", event.eventId, streamId, e.message, e)
            streamVersionCache.remove(streamId)
            throw e
        }
    }

    override fun readFromStream(streamId: UUID, fromVersion: Long, toVersion: Long?): List<DomainEvent> {
        val streamKey = getStreamKey(streamId)
        val range = Range.of(Range.Bound.inclusive("-"), Range.Bound.unbounded())

        val records = redisTemplate.opsForStream<String, String>().range(streamKey, range)
        val events = records?.mapNotNull { record ->
            try {
                serializer.deserialize(record.value)
            } catch (e: Exception) {
                logger.error("Error deserializing event from stream {}: {}", streamId, e.message, e)
                null
            }
        } ?: emptyList()

        return events.filter { it.version >= EventVersion(fromVersion) && (toVersion == null || it.version <= EventVersion(toVersion)) }
    }

    override fun getStreamVersion(streamId: UUID): Long {
        streamVersionCache[streamId]?.let { return it }
        val streamKey = getStreamKey(streamId)
        val size = redisTemplate.opsForStream<String, String>().size(streamKey) ?: 0L
        streamVersionCache[streamId] = size
        return size
    }

    private fun getStreamKey(streamId: UUID): String {
        return "${properties.streamPrefix}$streamId"
    }

    private fun getAllEventsStreamKey(): String {
        return "${properties.streamPrefix}${properties.allEventsStream}"
    }

    override fun readAllEvents(fromPosition: Long, maxCount: Int?): List<DomainEvent> {
        val allEventsStreamKey = getAllEventsStreamKey()
        val range = Range.of(Range.Bound.inclusive("-"), Range.Bound.unbounded())

        val records = redisTemplate.opsForStream<String, String>().range(allEventsStreamKey, range)
        val events = records?.mapNotNull { record ->
            try {
                serializer.deserialize(record.value)
            } catch (e: Exception) {
                logger.error("Error deserializing event from all events stream: {}", e.message, e)
                null
            }
        } ?: emptyList()

        val filteredEvents = events.drop(fromPosition.toInt())
        return if (maxCount != null && maxCount > 0) {
            filteredEvents.take(maxCount)
        } else {
            filteredEvents
        }
    }

    override fun subscribeToStream(streamId: UUID, fromVersion: Long, handler: (DomainEvent) -> Unit): Subscription {
        // Basic implementation - for full functionality, integrate with RedisEventConsumer
        logger.info("Stream subscription for streamId {} from version {} - basic implementation", streamId, fromVersion)
        return BasicSubscription {
            logger.info("Unsubscribed from stream {}", streamId)
        }
    }

    override fun subscribeToAll(fromPosition: Long, handler: (DomainEvent) -> Unit): Subscription {
        // Basic implementation - for full functionality, integrate with RedisEventConsumer
        logger.info("All events subscription from position {} - basic implementation", fromPosition)
        return BasicSubscription {
            logger.info("Unsubscribed from all events")
        }
    }
}

/**
 * Basic subscription implementation.
 */
private class BasicSubscription(
    private val unsubscribeAction: () -> Unit
) : Subscription {
    @Volatile
    private var active = true

    override fun unsubscribe() {
        if (active) {
            active = false
            unsubscribeAction()
        }
    }

    override fun isActive(): Boolean = active
}
