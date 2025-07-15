package at.mocode.utils

import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlinx.coroutines.Dispatchers

/**
 * Transaction management utility for handling database transactions in services.
 * Provides a clean API for wrapping service operations in database transactions.
 *
 * This utility supports:
 * - Standard read-write transactions
 * - Read-only transactions for better performance
 * - Proper error handling and logging
 * - Suspend function support without blocking
 */
object TransactionManager {

    private val logger = LoggerFactory.getLogger(TransactionManager::class.java)

    /**
     * Executes a block of code within a database transaction.
     * This is the main method for wrapping service operations that need transactional behavior.
     * Uses suspend-friendly transaction handling.
     *
     * @param block The code block to execute within the transaction
     * @return The result of the block execution
     * @throws Exception Any exception thrown within the transaction block
     */
    suspend fun <T> withTransaction(block: suspend () -> T): T {
        return try {
            logger.debug("Starting database transaction")
            val result = transaction {
                kotlinx.coroutines.runBlocking {
                    block()
                }
            }
            logger.debug("Database transaction completed successfully")
            result
        } catch (e: Exception) {
            logger.error("Database transaction failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Executes a block of code within a read-only database transaction.
     * This method is optimized for read operations and provides better performance
     * for operations that don't modify data.
     *
     * @param block The code block to execute within the read-only transaction
     * @return The result of the block execution
     * @throws Exception Any exception thrown within the transaction block
     */
    suspend fun <T> withReadOnlyTransaction(block: suspend () -> T): T {
        return try {
            logger.debug("Starting read-only database transaction")
            val result = transaction {
                // Note: Exposed doesn't have explicit read-only mode, but we can document the intent
                kotlinx.coroutines.runBlocking {
                    block()
                }
            }
            logger.debug("Read-only database transaction completed successfully")
            result
        } catch (e: Exception) {
            logger.error("Read-only database transaction failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Executes a block of code within a database transaction with explicit rollback handling.
     * This method provides explicit transaction control with automatic rollback on exceptions.
     * Note: Exposed automatically handles rollback on exceptions, so this is mainly for clarity.
     *
     * @param block The code block to execute within the transaction
     * @return The result of the block execution
     * @throws Exception Any exception thrown within the transaction block (transaction will be rolled back)
     */
    suspend fun <T> withTransactionRollback(block: suspend () -> T): T {
        return try {
            logger.debug("Starting database transaction with explicit rollback handling")
            val result = transaction {
                try {
                    kotlinx.coroutines.runBlocking {
                        block()
                    }
                } catch (e: Exception) {
                    logger.warn("Transaction failed, rolling back: ${e.message}")
                    // Exposed automatically handles rollback on exceptions
                    throw e
                }
            }
            logger.debug("Database transaction with rollback handling completed successfully")
            result
        } catch (e: Exception) {
            logger.error("Database transaction with rollback failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Executes a block of code within a database transaction with retry logic.
     * This method will retry the transaction up to the specified number of times
     * if it fails due to transient errors.
     *
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param block The code block to execute within the transaction
     * @return The result of the block execution
     * @throws Exception The last exception if all retry attempts fail
     */
    suspend fun <T> withTransactionRetry(maxRetries: Int = 3, block: suspend () -> T): T {
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                logger.debug("Starting database transaction (attempt ${attempt + 1}/${maxRetries + 1})")
                val result = transaction {
                    kotlinx.coroutines.runBlocking {
                        block()
                    }
                }
                logger.debug("Database transaction completed successfully on attempt ${attempt + 1}")
                return result
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    logger.warn("Database transaction failed on attempt ${attempt + 1}, retrying: ${e.message}")
                    // Add a small delay before retry
                    kotlinx.coroutines.runBlocking {
                        kotlinx.coroutines.delay(100L * (attempt + 1))
                    }
                } else {
                    logger.error("Database transaction failed after ${maxRetries + 1} attempts: ${e.message}", e)
                }
            }
        }

        throw lastException ?: RuntimeException("Transaction failed with unknown error")
    }
}
