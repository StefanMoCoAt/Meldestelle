package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import at.mocode.infrastructure.eventstore.api.Subscription
import com.benasher44.uuid.Uuid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.ConcurrentHashMap

class RedisEventStore(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) : EventStore {
    private val logger = LoggerFactory.getLogger(RedisEventStore::class.java)
    private val streamVersionCache = ConcurrentHashMap<Uuid, Long>()

    override fun appendToStream(events: List<DomainEvent>, streamId: Uuid, expectedVersion: Long): Long {
        if (events.isEmpty()) return getStreamVersion(streamId)

        val aggregateId = events.first().aggregateId
        require(events.all { it.aggregateId == aggregateId }) { "All events must belong to the same aggregate" }
        require(streamId == aggregateId) { "Stream ID must match aggregate ID" }

        var currentVersion = getStreamVersion(streamId)

        if (currentVersion != expectedVersion) {
            streamVersionCache.remove(streamId) // Invalidate cache on conflict
            val actualVersion = getStreamVersion(streamId) // Re-fetch from Redis
            if (actualVersion != expectedVersion) {
                throw ConcurrencyException("Concurrency conflict: expected version $expectedVersion but got $actualVersion")
            }
            currentVersion = actualVersion
        }

        for (event in events) {
            currentVersion = appendToStreamInternal(event, streamId, currentVersion)
        }
        return currentVersion
    }

    override fun appendToStream(event: DomainEvent, streamId: Uuid, expectedVersion: Long): Long {
        val currentVersion = getStreamVersion(streamId)
        if (currentVersion != expectedVersion) {
            streamVersionCache.remove(streamId)
            val actualVersion = getStreamVersion(streamId)
            if (actualVersion != expectedVersion) {
                throw ConcurrencyException("Concurrency conflict: expected version $expectedVersion but got $actualVersion")
            }
        }
        return appendToStreamInternal(event, streamId, expectedVersion)
    }

    private fun appendToStreamInternal(event: DomainEvent, streamId: Uuid, currentVersion: Long): Long {
        val newVersion = currentVersion + 1
        require(event.version == newVersion) { "Event version ${event.version} does not match expected new version $newVersion" }

        val streamKey = getStreamKey(streamId)
        val allEventsStreamKey = getAllEventsStreamKey()
        val eventData = serializer.serialize(event)

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
            return newVersion
        } catch (e: Exception) {
            logger.error("Failed to append event transactionally for stream key: {}", streamKey, e)
            streamVersionCache.remove(streamId)
            throw e
        }
    }

    override fun readFromStream(streamId: Uuid, fromVersion: Long, toVersion: Long?): List<DomainEvent> {
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

        return events.filter { it.version >= fromVersion && (toVersion == null || it.version <= toVersion) }
    }

    override fun getStreamVersion(streamId: Uuid): Long {
        streamVersionCache[streamId]?.let { return it }
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
        TODO("Not yet implemented")
    }

    override fun subscribeToStream(streamId: Uuid, fromVersion: Long, handler: (DomainEvent) -> Unit): Subscription {
        TODO("Not yet implemented")
    }

    override fun subscribeToAll(fromPosition: Long, handler: (DomainEvent) -> Unit): Subscription {
        TODO("Not yet implemented")
    }
}
