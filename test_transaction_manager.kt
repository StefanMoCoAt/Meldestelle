import at.mocode.utils.TransactionManager
import kotlinx.coroutines.runBlocking

/**
 * Simple test to verify TransactionManager functionality
 */
fun main() = runBlocking {
    println("[DEBUG_LOG] Testing TransactionManager functionality...")

    try {
        // Test basic transaction
        val result1 = TransactionManager.withTransaction {
            println("[DEBUG_LOG] Inside basic transaction")
            "Basic transaction completed"
        }
        println("[DEBUG_LOG] Result 1: $result1")

        // Test read-only transaction
        val result2 = TransactionManager.withReadOnlyTransaction {
            println("[DEBUG_LOG] Inside read-only transaction")
            "Read-only transaction completed"
        }
        println("[DEBUG_LOG] Result 2: $result2")

        // Test transaction with rollback
        val result3 = TransactionManager.withTransactionRollback {
            println("[DEBUG_LOG] Inside transaction with rollback handling")
            "Transaction with rollback completed"
        }
        println("[DEBUG_LOG] Result 3: $result3")

        println("[DEBUG_LOG] All TransactionManager tests completed successfully!")

    } catch (e: Exception) {
        println("[DEBUG_LOG] TransactionManager test failed: ${e.message}")
        e.printStackTrace()
    }
}
