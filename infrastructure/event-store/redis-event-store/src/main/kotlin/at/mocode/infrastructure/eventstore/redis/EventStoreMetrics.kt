package at.mocode.infrastructure.eventstore.redis

import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder

/**
 * Comprehensive metrics tracking for Redis Event Store operations.
 *
 * Tracks performance metrics, error rates, and operational statistics
 * to provide insights into event store health and performance.
 */
class EventStoreMetrics {
    private val logger = LoggerFactory.getLogger(EventStoreMetrics::class.java)

    // Operation counters
    private val appendOperations = LongAdder()
    private val appendBatchOperations = LongAdder()
    private val readOperations = LongAdder()
    private val subscriptionOperations = LongAdder()

    // Success/Error tracking
    private val successfulOperations = LongAdder()
    private val failedOperations = LongAdder()
    private val concurrencyExceptions = LongAdder()

    // Performance metrics
    private val totalOperationTime = LongAdder()
    private val maxOperationTime = AtomicLong(0)
    private val operationTimestamps = ConcurrentHashMap<String, Instant>()

    // Cache metrics
    private val cacheHits = LongAdder()
    private val cacheMisses = LongAdder()

    // Event statistics
    private val totalEventsAppended = LongAdder()
    private val totalEventsRead = LongAdder()

    private val lastMetricsReport = AtomicLong(System.currentTimeMillis())

    /**
     * Records the start of an operation for timing purposes.
     */
    fun startOperation(operationId: String) {
        operationTimestamps[operationId] = Instant.now()
    }

    /**
     * Records a successful append operation.
     */
    fun recordAppendSuccess(operationId: String, eventCount: Int = 1, isBatch: Boolean = false) {
        recordOperationEnd(operationId, true)
        appendOperations.increment()
        if (isBatch) appendBatchOperations.increment()
        totalEventsAppended.add(eventCount.toLong())

        logger.debug("[METRICS] Append operation completed successfully. Events: {}, Batch: {}", eventCount, isBatch)
    }

    /**
     * Records a failed append operation.
     */
    fun recordAppendFailure(operationId: String, error: Throwable? = null, isConcurrencyException: Boolean = false) {
        recordOperationEnd(operationId, false)
        if (isConcurrencyException) {
            concurrencyExceptions.increment()
        }

        logger.debug("[METRICS] Append operation failed. Concurrency conflict: {}, Error: {}",
                    isConcurrencyException, error?.message ?: "Unknown")
    }

    /**
     * Records a successful read operation.
     */
    fun recordReadSuccess(operationId: String, eventCount: Int) {
        recordOperationEnd(operationId, true)
        readOperations.increment()
        totalEventsRead.add(eventCount.toLong())

        logger.debug("[METRICS] Read operation completed successfully. Events: {}", eventCount)
    }

    /**
     * Records a failed read operation.
     */
    fun recordReadFailure(operationId: String, error: Throwable? = null) {
        recordOperationEnd(operationId, false)
        logger.debug("[METRICS] Read operation failed. Error: {}", error?.message ?: "Unknown")
    }

    /**
     * Records a cache hit.
     */
    fun recordCacheHit() {
        cacheHits.increment()
    }

    /**
     * Records a cache miss.
     */
    fun recordCacheMiss() {
        cacheMisses.increment()
    }

    /**
     * Records a subscription operation.
     */
    fun recordSubscription() {
        subscriptionOperations.increment()
        logger.debug("[METRICS] New subscription created")
    }

    private fun recordOperationEnd(operationId: String, success: Boolean) {
        val startTime = operationTimestamps.remove(operationId)
        if (startTime != null) {
            val duration = Duration.between(startTime, Instant.now())
            val durationMs = duration.toMillis()

            totalOperationTime.add(durationMs)
            maxOperationTime.updateAndGet { current -> maxOf(current, durationMs) }

            if (success) {
                successfulOperations.increment()
            } else {
                failedOperations.increment()
            }
        }
    }

    /**
     * Gets comprehensive metrics summary.
     */
    fun getMetrics(): EventStoreMetricsSnapshot {
        val totalOps = successfulOperations.sum() + failedOperations.sum()
        val successRate = if (totalOps > 0) (successfulOperations.sum().toDouble() / totalOps * 100) else 0.0
        val avgOperationTime = if (totalOps > 0) (totalOperationTime.sum().toDouble() / totalOps) else 0.0
        val cacheHitRate = run {
            val totalCacheOps = cacheHits.sum() + cacheMisses.sum()
            if (totalCacheOps > 0) (cacheHits.sum().toDouble() / totalCacheOps * 100) else 0.0
        }

        return EventStoreMetricsSnapshot(
            totalOperations = totalOps,
            successfulOperations = successfulOperations.sum(),
            failedOperations = failedOperations.sum(),
            successRate = successRate,
            appendOperations = appendOperations.sum(),
            batchAppendOperations = appendBatchOperations.sum(),
            readOperations = readOperations.sum(),
            subscriptionOperations = subscriptionOperations.sum(),
            concurrencyExceptions = concurrencyExceptions.sum(),
            totalEventsAppended = totalEventsAppended.sum(),
            totalEventsRead = totalEventsRead.sum(),
            averageOperationTimeMs = avgOperationTime,
            maxOperationTimeMs = maxOperationTime.get(),
            cacheHits = cacheHits.sum(),
            cacheMisses = cacheMisses.sum(),
            cacheHitRate = cacheHitRate
        )
    }

    /**
     * Logs performance metrics if enough time has passed since the last report.
     */
    fun logPerformanceMetrics() {
        val now = System.currentTimeMillis()
        val lastReport = lastMetricsReport.get()

        // Log metrics every 5 minutes
        if (now - lastReport > 300_000) {
            if (lastMetricsReport.compareAndSet(lastReport, now)) {
                val metrics = getMetrics()
                logger.info("[PERFORMANCE_METRICS] {}", metrics.toLogString())
            }
        }
    }

    /**
     * Resets all metrics. Useful for testing.
     */
    internal fun reset() {
        appendOperations.reset()
        appendBatchOperations.reset()
        readOperations.reset()
        subscriptionOperations.reset()
        successfulOperations.reset()
        failedOperations.reset()
        concurrencyExceptions.reset()
        totalOperationTime.reset()
        maxOperationTime.set(0)
        operationTimestamps.clear()
        cacheHits.reset()
        cacheMisses.reset()
        totalEventsAppended.reset()
        totalEventsRead.reset()
        lastMetricsReport.set(System.currentTimeMillis())
    }
}

/**
 * Immutable snapshot of event store metrics at a point in time.
 */
data class EventStoreMetricsSnapshot(
    val totalOperations: Long,
    val successfulOperations: Long,
    val failedOperations: Long,
    val successRate: Double,
    val appendOperations: Long,
    val batchAppendOperations: Long,
    val readOperations: Long,
    val subscriptionOperations: Long,
    val concurrencyExceptions: Long,
    val totalEventsAppended: Long,
    val totalEventsRead: Long,
    val averageOperationTimeMs: Double,
    val maxOperationTimeMs: Long,
    val cacheHits: Long,
    val cacheMisses: Long,
    val cacheHitRate: Double
) {
    fun toLogString(): String {
        return "EventStore Metrics: " +
                "Operations=${totalOperations}, " +
                "Success Rate=${String.format("%.1f%%", successRate)}, " +
                "Appends=${appendOperations} (${batchAppendOperations} batches), " +
                "Reads=${readOperations}, " +
                "Subscriptions=${subscriptionOperations}, " +
                "Events Appended=${totalEventsAppended}, " +
                "Events Read=${totalEventsRead}, " +
                "Avg Time=${String.format("%.1fms", averageOperationTimeMs)}, " +
                "Max Time=${maxOperationTimeMs}ms, " +
                "Cache Hit Rate=${String.format("%.1f%%", cacheHitRate)}, " +
                "Concurrency Conflicts=${concurrencyExceptions}"
    }
}
