@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

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
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class RedisEventStore(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) : EventStore {
    private val logger = LoggerFactory.getLogger(RedisEventStore::class.java)
    private val streamVersionCache = ConcurrentHashMap<Uuid, Long>()
    private val metrics = EventStoreMetrics()

    override fun appendToStream(events: List<DomainEvent>, streamId: Uuid, expectedVersion: Long): Long {
        val operationId = "batch-append-${System.nanoTime()}"
        metrics.startOperation(operationId)

        try {
            if (events.isEmpty()) {
                logger.debug("Empty event list provided for stream {}, returning current version", streamId)
                val version = getStreamVersion(streamId)
                metrics.recordAppendSuccess(operationId, 0, true)
                return version
            }

            val aggregateId = events.first().aggregateId
            require(events.all { it.aggregateId == aggregateId }) {
                "All events must belong to the same aggregate. Expected: $aggregateId, but found mixed aggregate IDs"
            }
            require(streamId == aggregateId.value) {
                "Stream ID $streamId must match aggregate ID ${aggregateId.value}"
            }

            logger.debug("Appending {} events to stream {} with expected version {}", events.size, streamId, expectedVersion)

            val currentVersion = validateAndGetCurrentVersion(streamId, expectedVersion)
            val newVersion = appendEventsInBatch(events, streamId, currentVersion)

            logger.info("Successfully appended {} events to stream {}. New version: {}", events.size, streamId, newVersion)
            metrics.recordAppendSuccess(operationId, events.size, true)
            metrics.logPerformanceMetrics()
            return newVersion

        } catch (e: ConcurrencyException) {
            metrics.recordAppendFailure(operationId, e, true)
            throw e
        } catch (e: Exception) {
            metrics.recordAppendFailure(operationId, e, false)
            throw e
        }
    }

    override fun appendToStream(event: DomainEvent, streamId: Uuid, expectedVersion: Long): Long {
        val operationId = "single-append-${System.nanoTime()}"
        metrics.startOperation(operationId)

        try {
            logger.debug("Appending single event to stream {} with expected version {}", streamId, expectedVersion)
            val currentVersion = validateAndGetCurrentVersion(streamId, expectedVersion)
            val newVersion = appendToStreamInternal(event, streamId, currentVersion)

            logger.info("Successfully appended event to stream {}. New version: {}", streamId, newVersion)
            metrics.recordAppendSuccess(operationId, 1, false)
            metrics.logPerformanceMetrics()
            return newVersion

        } catch (e: ConcurrencyException) {
            metrics.recordAppendFailure(operationId, e, true)
            throw e
        } catch (e: Exception) {
            metrics.recordAppendFailure(operationId, e, false)
            throw e
        }
    }

    /**
     * Validiert die erwartete Version und gibt die aktuelle Version zurück, behandelt Cache-Invalidierung bei Konflikten.
     */
    private fun validateAndGetCurrentVersion(streamId: Uuid, expectedVersion: Long): Long {
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

        return currentVersion
    }

    /**
     * Fügt mehrere Events in einer einzigen Redis-Transaktion hinzu für optimale Performance.
     */
    private fun appendEventsInBatch(events: List<DomainEvent>, streamId: Uuid, currentVersion: Long): Long {
        val streamKey = getStreamKey(streamId)
        val allEventsStreamKey = getAllEventsStreamKey()

        // Validate all events have correct sequential versions
        events.forEachIndexed { index, event ->
            val expectedVersion = currentVersion + index + 1
            require(event.version.value == expectedVersion) {
                "Event ${index} version ${event.version.value} does not match expected version $expectedVersion for stream $streamId"
            }
        }

        logger.debug("Writing {} events to stream {} and all-events stream in single transaction", events.size, streamId)

        try {
            redisTemplate.execute(object : SessionCallback<List<Any>> {
                @Throws(DataAccessException::class)
                override fun <K : Any, V : Any> execute(operations: org.springframework.data.redis.core.RedisOperations<K, V>): List<Any> {
                    val streamOps = (operations as StringRedisTemplate).opsForStream<String, String>()

                    operations.multi()

                    // Add all events to both streams in a single transaction
                    events.forEach { event ->
                        val eventData = serializer.serialize(event)
                        streamOps.add(streamKey, eventData)
                        streamOps.add(allEventsStreamKey, eventData)
                    }

                    return operations.exec()
                }
            })

            val newVersion = currentVersion + events.size
            streamVersionCache[streamId] = newVersion
            logger.debug("Successfully wrote {} events to Redis streams in batch, updated cache version to {}", events.size, newVersion)
            return newVersion

        } catch (e: Exception) {
            logger.error("Failed to append {} events in batch for stream {}: {}", events.size, streamId, e.message, e)
            streamVersionCache.remove(streamId)
            throw e
        }
    }

    private fun appendToStreamInternal(event: DomainEvent, streamId: Uuid, currentVersion: Long): Long {
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
                override fun <K : Any, V : Any> execute(operations: org.springframework.data.redis.core.RedisOperations<K, V>): List<Any> {
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

    override fun readFromStream(streamId: Uuid, fromVersion: Long, toVersion: Long?): List<DomainEvent> {
        val operationId = "read-stream-${System.nanoTime()}"
        metrics.startOperation(operationId)

        try {
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

            val filteredEvents = events.filter { it.version >= EventVersion(fromVersion) && (toVersion == null || it.version <= EventVersion(toVersion)) }

            metrics.recordReadSuccess(operationId, filteredEvents.size)
            return filteredEvents

        } catch (e: Exception) {
            metrics.recordReadFailure(operationId, e)
            throw e
        }
    }

    override fun getStreamVersion(streamId: Uuid): Long {
        streamVersionCache[streamId]?.let {
            metrics.recordCacheHit()
            return it
        }

        metrics.recordCacheMiss()
        val streamKey = getStreamKey(streamId)
        val size = redisTemplate.opsForStream<String, String>().size(streamKey) ?: 0L
        streamVersionCache[streamId] = size
        return size
    }

    private fun getStreamKey(streamId: Uuid): String {
        return "${properties.streamPrefix}$streamId"
    }

    private fun getAllEventsStreamKey(): String {
        return "${properties.streamPrefix}${properties.allEventsStream}"
    }

    override fun readAllEvents(fromPosition: Long, maxCount: Int?): List<DomainEvent> {
        val operationId = "read-all-events-${System.nanoTime()}"
        metrics.startOperation(operationId)

        try {
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
            val result = if (maxCount != null && maxCount > 0) {
                filteredEvents.take(maxCount)
            } else {
                filteredEvents
            }

            metrics.recordReadSuccess(operationId, result.size)
            return result

        } catch (e: Exception) {
            metrics.recordReadFailure(operationId, e)
            throw e
        }
    }

    override fun subscribeToStream(streamId: Uuid, fromVersion: Long, handler: (DomainEvent) -> Unit): Subscription {
        // Basic implementation - for full functionality, integrate with RedisEventConsumer
        logger.info("Stream subscription for streamId {} from version {} - basic implementation", streamId, fromVersion)
        metrics.recordSubscription()
        return BasicSubscription {
            logger.info("Unsubscribed from stream {}", streamId)
        }
    }

    override fun subscribeToAll(fromPosition: Long, handler: (DomainEvent) -> Unit): Subscription {
        // Basic implementation - for full functionality, integrate with RedisEventConsumer
        logger.info("All events subscription from position {} - basic implementation", fromPosition)
        metrics.recordSubscription()
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
