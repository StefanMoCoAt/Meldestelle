package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Consumer for Redis Streams that processes events using consumer groups.
 */
class RedisEventConsumer(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) {
    private val logger = LoggerFactory.getLogger(RedisEventConsumer::class.java)
    private val eventTypeHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<(DomainEvent) -> Unit>>()
    private val allEventHandlers = CopyOnWriteArrayList<(DomainEvent) -> Unit>()
    private var running = false

    /**
     * Initializes the consumer.
     */
    @PostConstruct
    fun init() {
        if (properties.createConsumerGroupIfNotExists) {
            createConsumerGroupsIfNotExist()
        }
    }

    /**
     * Stops the consumer.
     */
    @PreDestroy
    fun shutdown() {
        running = false
    }

    /**
     * Registers a handler for a specific event type.
     *
     * @param eventType The type of event to handle
     * @param handler The handler to call when an event of the specified type is received
     */
    fun registerEventHandler(eventType: String, handler: (DomainEvent) -> Unit) {
        eventTypeHandlers.computeIfAbsent(eventType) { CopyOnWriteArrayList() }.add(handler)
        logger.debug("Registered handler for event type: $eventType")
    }

    /**
     * Registers a handler for all events.
     *
     * @param handler The handler to call when any event is received
     */
    fun registerAllEventsHandler(handler: (DomainEvent) -> Unit) {
        allEventHandlers.add(handler)
        logger.debug("Registered handler for all events")
    }

    /**
     * Unregisters a handler for a specific event type.
     *
     * @param eventType The type of event
     * @param handler The handler to unregister
     */
    fun unregisterEventHandler(eventType: String, handler: (DomainEvent) -> Unit) {
        eventTypeHandlers[eventType]?.remove(handler)
        logger.debug("Unregistered handler for event type: $eventType")
    }

    /**
     * Unregisters a handler for all events.
     *
     * @param handler The handler to unregister
     */
    fun unregisterAllEventsHandler(handler: (DomainEvent) -> Unit) {
        allEventHandlers.remove(handler)
        logger.debug("Unregistered handler for all events")
    }

    /**
     * Creates consumer groups for all streams if they don't exist.
     */
    private fun createConsumerGroupsIfNotExist() {
        try {
            val allEventsStreamKey = getAllEventsStreamKey()
            try {
                redisTemplate.opsForStream<String, String>()
                    .add(allEventsStreamKey, mapOf("init" to "init"))
                logger.debug("Ensured all-events stream has messages: $allEventsStreamKey")
            } catch (e: Exception) {
                logger.debug("All-events stream might already have messages: ${e.message}")
            }

            createConsumerGroupIfNotExists(allEventsStreamKey)

            val streamKeys = redisTemplate.keys("${properties.streamPrefix}*")

            for (streamKey in streamKeys) {
                if (streamKey != allEventsStreamKey) {
                    createConsumerGroupIfNotExists(streamKey)
                }
            }
        } catch (e: Exception) {
            logger.error("Error creating consumer groups: ${e.message}", e)
        }
    }

    /**
     * Creates a consumer group for a stream if it doesn't exist.
     *
     * @param streamKey The key of the stream
     */
    private fun createConsumerGroupIfNotExists(streamKey: String) {
        try {
            try {
                redisTemplate.opsForStream<String, String>()
                    .add(streamKey, mapOf("init" to "init"))
                logger.debug("Ensured stream has messages: $streamKey")
            } catch (e: Exception) {
                logger.debug("Stream $streamKey might already have messages: ${e.message}")
            }

            try {
                redisTemplate.opsForStream<String, String>()
                    .createGroup(streamKey, ReadOffset.latest(), properties.consumerGroup)
                logger.debug("Created consumer group ${properties.consumerGroup} for stream: $streamKey")
            } catch (e: Exception) {
                logger.debug("Could not create consumer group ${properties.consumerGroup} for stream: $streamKey: ${e.message}")
            }
        } catch (e: Exception) {
            logger.error("Error creating consumer group for stream $streamKey: ${e.message}", e)
        }
    }

    /**
     * Periodic polls for new events from all streams.
     */
    @Scheduled(fixedDelayString = $$"${redis.event-store.poll-interval:100}")
    fun pollEvents() {
        if (!running) {
            running = true
        }

        try {
            pollStream(getAllEventsStreamKey())
            claimPendingMessages()
        } catch (e: Exception) {
            logger.error("Error polling events: ${e.message}", e)
        }
    }

    /**
     * Polls a stream for new events.
     *
     * @param streamKey The key of the stream to poll
     */
    private fun pollStream(streamKey: String) {
        try {
            val options = StreamReadOptions.empty()
                .count(properties.maxBatchSize.toLong())
                .block(properties.pollTimeout)

            val records = redisTemplate.opsForStream<String, String>()
                .read(
                    Consumer.from(properties.consumerGroup, properties.consumerName),
                    options,
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                )

            if (records != null) {
                for (record in records) {
                    processRecord(record)
                }
            }
        } catch (e: Exception) {
            val message = e.message
            if (message == null || !message.contains("NOGROUP")) {
                logger.error("Error polling stream $streamKey: ${e.message}", e)
            }
        }
    }

    /**
     * Claims pending messages that have been idle for too long.
     */
    private fun claimPendingMessages() {
        try {
            val streamKey = getAllEventsStreamKey()

            val pendingSummary = redisTemplate.opsForStream<String, String>()
                .pending(streamKey, properties.consumerGroup)

            if (pendingSummary != null && pendingSummary.totalPendingMessages > 0) {
                val pendingMessages = redisTemplate.opsForStream<String, String>()
                    .pending(
                        streamKey,
                        Consumer.from(properties.consumerGroup, properties.consumerName),
                        Range.unbounded<String>(),
                        properties.maxBatchSize.toLong()
                    )

                if (pendingMessages.size() > 0) {
                    val messageIdsList = pendingMessages.map { it.id }.toList()

                    if (messageIdsList.isNotEmpty()) {
                        val messageIds = messageIdsList.toTypedArray()

                        val records = redisTemplate.opsForStream<String, String>()
                            .claim(
                                streamKey,
                                properties.consumerGroup,
                                properties.consumerName,
                                properties.claimIdleTimeout,
                                *messageIds
                            )

                        for (record in records) {
                            processRecord(record)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error claiming pending messages: ${e.message}", e)
        }
    }

    /**
     * Processes a record from a stream.
     *
     * @param record The record to process
     */
    private fun processRecord(record: MapRecord<String, String, String>) {
        try {
            val data = record.value

            if (data.size == 1 && data.containsKey("init") && data["init"] == "init") {
                logger.debug("Skipping init message")
                redisTemplate.opsForStream<String, String>()
                    .acknowledge(properties.consumerGroup, record)
                return
            }

            val event = serializer.deserialize(data)
            val eventType = serializer.getEventType(data)

            eventTypeHandlers[eventType]?.forEach { handler ->
                try {
                    handler(event)
                } catch (e: Exception) {
                    logger.error("Error handling event of type $eventType: ${e.message}", e)
                }
            }

            allEventHandlers.forEach { handler ->
                try {
                    handler(event)
                } catch (e: Exception) {
                    logger.error("Error handling event: ${e.message}", e)
                }
            }

            redisTemplate.opsForStream<String, String>()
                .acknowledge(properties.consumerGroup, record)

        } catch (e: Exception) {
            logger.error("Error processing record: ${e.message}", e)
        }
    }

    /**
     * Gets the Redis key for the all-events stream.
     *
     * @return The Redis key for the all-events stream
     */
    private fun getAllEventsStreamKey(): String {
        return "${properties.streamPrefix}${properties.allEventsStream}"
    }
}
