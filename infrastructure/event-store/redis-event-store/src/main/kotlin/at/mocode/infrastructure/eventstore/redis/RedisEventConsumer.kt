package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Consumer for Redis Streams that processes events using consumer groups.
 */
class RedisEventConsumer(
    private val redisTemplate: StringRedisTemplate,
    private val serializer: EventSerializer,
    private val properties: RedisEventStoreProperties
) {
    private val logger = LoggerFactory.getLogger(RedisEventConsumer::class.java)

    // Event handlers registered for specific event types
    private val eventTypeHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<(DomainEvent) -> Unit>>()

    // Event handlers registered for all events
    private val allEventHandlers = CopyOnWriteArrayList<(DomainEvent) -> Unit>()

    // Flag to indicate if the consumer is running
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
            // Create consumer group for the all events stream
            val allEventsStreamKey = getAllEventsStreamKey()

            // Ensure the all-events stream exists and has at least one message
            try {
                // Always try to add an initialization message to the all-events stream
                redisTemplate.opsForStream<String, String>()
                    .add(allEventsStreamKey, mapOf("init" to "init"))
                logger.debug("Ensured all-events stream has messages: $allEventsStreamKey")
            } catch (e: Exception) {
                // Ignore errors when adding to the stream (it might already have messages)
                logger.debug("All-events stream might already have messages: ${e.message}")
            }

            // Create the consumer group for all-events stream
            createConsumerGroupIfNotExists(allEventsStreamKey)

            // Get all stream keys
            val streamKeys = redisTemplate.keys("${properties.streamPrefix}*")

            // Create consumer groups for all streams
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
            // Always ensure the stream has at least one message
            // This is necessary because consumer groups cannot be created on empty streams
            try {
                redisTemplate.opsForStream<String, String>()
                    .add(streamKey, mapOf("init" to "init"))
                logger.debug("Ensured stream has messages: $streamKey")
            } catch (e: Exception) {
                // Ignore errors when adding to the stream (it might already have messages)
                logger.debug("Stream $streamKey might already have messages: ${e.message}")
            }

            // Create the consumer group - ignore all errors for now
            try {
                redisTemplate.opsForStream<String, String>()
                    .createGroup(streamKey, ReadOffset.latest(), properties.consumerGroup)
                logger.debug("Created consumer group ${properties.consumerGroup} for stream: $streamKey")
            } catch (e: Exception) {
                // Ignore all consumer group creation errors for now
                logger.debug("Could not create consumer group ${properties.consumerGroup} for stream: $streamKey: ${e.message}")
            }
        } catch (e: Exception) {
            logger.error("Error creating consumer group for stream $streamKey: ${e.message}", e)
        }
    }

    /**
     * Periodically polls for new events from all streams.
     */
    @Scheduled(fixedDelayString = "\${redis.event-store.poll-interval:100}")
    fun pollEvents() {
        if (!running) {
            running = true
        }

        try {
            // Poll the all events stream only
            // Individual streams don't need to be polled since all events are also in the all-events stream
            pollStream(getAllEventsStreamKey())

            // Claim pending messages that have been idle for too long
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
            // Read new messages from the stream
            val options = StreamReadOptions.empty()
                .count(properties.maxBatchSize.toLong())
                .block(properties.pollTimeout)

            val records = redisTemplate.opsForStream<String, String>()
                .read(
                    Consumer.from(properties.consumerGroup, properties.consumerName),
                    options,
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                )

            // Process the records
            if (records != null) {
                for (record in records) {
                    processRecord(record)
                }
            }
        } catch (e: Exception) {
            // Ignore if the stream doesn't exist or the consumer group doesn't exist
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
            // Only process the all-events stream since that's where consumer groups exist
            val streamKey = getAllEventsStreamKey()

            // Get pending messages summary
            val pendingSummary = redisTemplate.opsForStream<String, String>()
                .pending(streamKey, properties.consumerGroup)

            if (pendingSummary != null && pendingSummary.totalPendingMessages > 0) {
                // Get pending messages with details
                val pendingMessages = redisTemplate.opsForStream<String, String>()
                    .pending(
                        streamKey,
                        Consumer.from(properties.consumerGroup, properties.consumerName),
                        Range.unbounded<String>(),
                        properties.maxBatchSize.toLong()
                    )

                if (pendingMessages.size() > 0) {
                    // Extract message IDs and convert to array
                    val messageIdsList = pendingMessages.map { it.id }.toList()

                    if (messageIdsList.isNotEmpty()) {
                        // Convert to array for the spread operator
                        val messageIds = messageIdsList.toTypedArray()

                        // Claim messages that have been idle for too long
                        val records = redisTemplate.opsForStream<String, String>()
                            .claim(
                                streamKey,
                                properties.consumerGroup,
                                properties.consumerName,
                                properties.claimIdleTimeout,
                                *messageIds
                            )

                        // Process the claimed records
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

            // Skip init messages (they only contain "init" -> "init")
            if (data.size == 1 && data.containsKey("init") && data["init"] == "init") {
                logger.debug("Skipping init message")
                // Still acknowledge the message to remove it from pending
                redisTemplate.opsForStream<String, String>()
                    .acknowledge(properties.consumerGroup, record)
                return
            }

            val event = serializer.deserialize(data)
            val eventType = serializer.getEventType(data)

            // Call handlers for the specific event type
            eventTypeHandlers[eventType]?.forEach { handler ->
                try {
                    handler(event)
                } catch (e: Exception) {
                    logger.error("Error handling event of type $eventType: ${e.message}", e)
                }
            }

            // Call handlers for all events
            allEventHandlers.forEach { handler ->
                try {
                    handler(event)
                } catch (e: Exception) {
                    logger.error("Error handling event: ${e.message}", e)
                }
            }

            // Acknowledge the message
            redisTemplate.opsForStream<String, String>()
                .acknowledge(properties.consumerGroup, record)

        } catch (e: Exception) {
            logger.error("Error processing record: ${e.message}", e)
        }
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
