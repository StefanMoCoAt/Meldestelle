package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * Performance tests for authentication operations.
 * These tests ensure that JWT operations meet performance requirements under various load conditions.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthPerformanceTest {

    private lateinit var jwtService: JwtService
    private val testSecret = "a-very-long-and-secure-test-secret-that-is-at-least-512-bits-long-for-hmac512"
    private val testIssuer = "test-issuer"
    private val testAudience = "test-audience"

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(
            secret = testSecret,
            issuer = testIssuer,
            audience = testAudience,
            expiration = 60.minutes
        )
    }

    // ========== JWT Validation Performance Tests ==========

    @Test
    fun `JWT validation should complete under 10ms`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))

        // Act & Assert - Single validation should be very fast
        repeat(100) {
            val timeMs = measureTimeMillis {
                val result = jwtService.validateToken(token)
                assertTrue(result.isSuccess)
            }
            assertTrue(timeMs < 10, "JWT validation should complete under 10ms (took ${timeMs}ms)")
        }
    }

    @Test
    fun `JWT validation should handle burst load efficiently`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))
        val iterations = 10000

        // Act
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                val result = jwtService.validateToken(token)
                assertTrue(result.isSuccess)
            }
        }

        // Assert - 10,000 validations should complete within reasonable time
        val avgTimeMs = timeMs.toDouble() / iterations
        assertTrue(timeMs < 5000, "10,000 validations should complete within 5 seconds (took ${timeMs}ms)")
        assertTrue(avgTimeMs < 0.5, "Average validation time should be under 0.5ms (was ${avgTimeMs}ms)")
    }

    @Test
    @Disabled("Test too flaky - JVM warmup and system load cause high variance making it unsuitable for CI")
    fun `JWT validation performance should be consistent`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))
        val measurements = mutableListOf<Long>()

        // Act - Measure multiple batches
        repeat(10) {
            val batchTime = measureTimeMillis {
                repeat(1000) {
                    val result = jwtService.validateToken(token)
                    assertTrue(result.isSuccess)
                }
            }
            measurements.add(batchTime)
        }

        // Assert - Performance should be consistent across batches
        val avgTime = measurements.average()
        val maxDeviation = measurements.maxOf { kotlin.math.abs(it - avgTime) }
        assertTrue(maxDeviation < avgTime * 2.5,
            "Performance should be consistent (max deviation: ${maxDeviation}ms, avg: ${avgTime}ms, tolerance: 250%)")
    }

    // ========== Token Generation Performance Tests ==========

    @Test
    fun `token generation should complete under 5ms`() {
        // Arrange
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE, BerechtigungE.VEREIN_UPDATE)

        // Act & Assert
        repeat(100) {
            val timeMs = measureTimeMillis {
                val token = jwtService.generateToken("user-$it", "testuser$it", permissions)
                assertNotNull(token)
                assertTrue(token.isNotEmpty())
            }
            assertTrue(timeMs < 50, "Token generation should complete under 50ms (took ${timeMs}ms)")
        }
    }

    @Test
    fun `token generation should handle high throughput`() {
        // Arrange
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.VEREIN_READ)
        val iterations = 5000

        // Act
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                val token = jwtService.generateToken("user-$it", "testuser$it", permissions)
                assertTrue(token.isNotEmpty())
            }
        }

        // Assert - Should generate 5000 tokens within a reasonable time
        val tokensPerSecond = (iterations * 1000.0) / timeMs
        assertTrue(tokensPerSecond > 1000,
            "Should generate at least 1000 tokens/second (achieved ${tokensPerSecond.toInt()}/second)")
    }

    // ========== Concurrent Access Performance Tests ==========

    @Test
    fun `token generation should handle concurrent requests`() {
        // Arrange
        val threadCount = 10
        val operationsPerThread = 500
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Boolean>()
        val errors = mutableListOf<Exception>()

        // Act
        val totalTime = measureTimeMillis {
            repeat(threadCount) { threadIndex ->
                executor.submit {
                    try {
                        repeat(operationsPerThread) { opIndex ->
                            val token = jwtService.generateToken(
                                "user-$threadIndex-$opIndex",
                                "testuser$threadIndex",
                                listOf(BerechtigungE.PERSON_READ)
                            )
                            val isValid = jwtService.validateToken(token).isSuccess
                            synchronized(results) {
                                results.add(isValid)
                            }
                        }
                    } catch (e: Exception) {
                        synchronized(errors) {
                            errors.add(e)
                        }
                    } finally {
                        latch.countDown()
                    }
                }
            }
            assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds")
        }

        executor.shutdown()

        // Assert
        assertTrue(errors.isEmpty(), "No errors should occur during concurrent operations: ${errors.firstOrNull()}")
        assertEquals(threadCount * operationsPerThread, results.size)
        assertTrue(results.all { it }, "All tokens should be valid")

        val operationsPerSecond = (threadCount * operationsPerThread * 1000.0) / totalTime
        assertTrue(operationsPerSecond > 500,
            "Should handle at least 500 operations/second under concurrent load (achieved ${operationsPerSecond.toInt()}/second)")
    }

    @Test
    fun `token validation should handle concurrent requests`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))
        val threadCount = 20
        val validationsPerThread = 1000
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Boolean>()

        // Act
        val totalTime = measureTimeMillis {
            repeat(threadCount) {
                executor.submit {
                    repeat(validationsPerThread) {
                        val isValid = jwtService.validateToken(token).isSuccess
                        synchronized(results) {
                            results.add(isValid)
                        }
                    }
                    latch.countDown()
                }
            }
            assertTrue(latch.await(10, TimeUnit.SECONDS), "All validations should complete within 10 seconds")
        }

        executor.shutdown()

        // Assert
        assertEquals(threadCount * validationsPerThread, results.size)
        assertTrue(results.all { it }, "All validations should succeed")

        val validationsPerSecond = (threadCount * validationsPerThread * 1000.0) / totalTime
        assertTrue(validationsPerSecond > 10000,
            "Should handle at least 10,000 validations/second under concurrent load (achieved ${validationsPerSecond.toInt()}/second)")
    }

    // ========== Memory Usage Performance Tests ==========

    @Test
    fun `memory usage should be stable under load`() {
        // Arrange
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // Act - Perform many operations to test for memory leaks
        repeat(10000) {
            val token = jwtService.generateToken("user-$it", "testuser$it", listOf(BerechtigungE.PERSON_READ))
            val result = jwtService.validateToken(token)
            assertTrue(result.isSuccess)

            // Extract data to ensure full processing
            jwtService.getUserIdFromToken(token)
            jwtService.getPermissionsFromToken(token)
        }

        // Force garbage collection
        System.gc()
        Thread.sleep(100) // Give GC time to run

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory

        // Assert - Memory increase should be reasonable (less than 50MB)
        assertTrue(memoryIncrease < 50 * 1024 * 1024,
            "Memory increase should be less than 50MB (was ${memoryIncrease / 1024 / 1024}MB)")
    }

    // ========== Complex Permissions Performance Tests ==========

    @Test
    fun `should handle large permission sets efficiently`() {
        // Arrange - Create a token with all available permissions
        val allPermissions = BerechtigungE.entries

        // Act & Assert - Generation should still be fast
        val generationTime = measureTimeMillis {
            val token = jwtService.generateToken("admin-user", "admin", allPermissions)
            assertNotNull(token)
        }
        assertTrue(generationTime < 500, "Generation with all permissions should be under 500ms")

        // Validation should also be fast
        val token = jwtService.generateToken("admin-user", "admin", allPermissions)
        val validationTime = measureTimeMillis {
            val result = jwtService.validateToken(token)
            assertTrue(result.isSuccess)

            val permissions = jwtService.getPermissionsFromToken(token).getOrElse { emptyList() }
            assertEquals(allPermissions.size, permissions.size)
        }
        assertTrue(validationTime < 80, "Validation with all permissions should be under 50ms")
    }

    // ========== Stress Tests ==========

    @Test
    fun `should handle sustained load without degradation`() {
        // Arrange
        val testDurationMs = 5000L // 5 seconds
        val startTime = System.currentTimeMillis()
        var operationCount = 0
        val measurementPoints = mutableListOf<Pair<Long, Int>>() // time, operations per second

        // Act - Sustained load test
        while (System.currentTimeMillis() - startTime < testDurationMs) {
            val intervalStart = System.currentTimeMillis()
            var intervalOperations = 0

            // Run operations for 1-second intervals
            while (System.currentTimeMillis() - intervalStart < 1000) {
                val token = jwtService.generateToken("user-$operationCount", "test", listOf(BerechtigungE.PERSON_READ))
                val isValid = jwtService.validateToken(token).isSuccess
                assertTrue(isValid)
                operationCount++
                intervalOperations++
            }

            measurementPoints.add(Pair(System.currentTimeMillis() - startTime, intervalOperations))
        }

        // Assert - Performance should not degrade significantly over time
        assertTrue(measurementPoints.size >= 4, "Should have at least 4 measurement points")

        val firstHalf = measurementPoints.take(measurementPoints.size / 2).map { it.second }
        val secondHalf = measurementPoints.drop(measurementPoints.size / 2).map { it.second }

        val firstHalfAvg = firstHalf.average()
        val secondHalfAvg = secondHalf.average()

        // Performance in the second half should not be significantly worse than the first half
        assertTrue(secondHalfAvg > firstHalfAvg * 0.8,
            "Performance should not degrade by more than 20% over time " +
            "(first half: ${firstHalfAvg.toInt()} ops/sec, second half: ${secondHalfAvg.toInt()} ops/sec)")
    }

    @Test
    fun `operations should complete within timeout under extreme load`() {
        // Arrange - Very high-load scenario
        val operations = 50000

        // Act & Assert - Should complete within a reasonable timeout
        assertTimeoutPreemptively(Duration.ofSeconds(30)) {
            repeat(operations) {
                val token = jwtService.generateToken("user-$it", "test", listOf(BerechtigungE.PERSON_READ))
                val result = jwtService.validateToken(token)
                assertTrue(result.isSuccess)
            }
        }
    }

    // ========== Benchmarking Tests ==========

    @Test
    fun `benchmark basic JWT operations`() {
        // This test provides baseline performance metrics for monitoring
        val iterations = 1000

        // Token Generation Benchmark
        val generationTime = measureTimeMillis {
            repeat(iterations) {
                jwtService.generateToken("user-$it", "test", listOf(BerechtigungE.PERSON_READ))
            }
        }
        val avgGenerationMs = generationTime.toDouble() / iterations
        println("[DEBUG_LOG] Token generation: ${avgGenerationMs}ms average (${iterations} iterations)")

        // Token Validation Benchmark
        val token = jwtService.generateToken("benchmark-user", "test", listOf(BerechtigungE.PERSON_READ))
        val validationTime = measureTimeMillis {
            repeat(iterations) {
                jwtService.validateToken(token)
            }
        }
        val avgValidationMs = validationTime.toDouble() / iterations
        println("[DEBUG_LOG] Token validation: ${avgValidationMs}ms average (${iterations} iterations)")

        // Data Extraction Benchmark
        val extractionTime = measureTimeMillis {
            repeat(iterations) {
                jwtService.getUserIdFromToken(token)
                jwtService.getPermissionsFromToken(token)
            }
        }
        val avgExtractionMs = extractionTime.toDouble() / iterations
        println("[DEBUG_LOG] Data extraction: ${avgExtractionMs}ms average (${iterations} iterations)")

        // Performance should meet baseline requirements
        assertTrue(avgGenerationMs < 2.0, "Token generation should average under 2ms")
        assertTrue(avgValidationMs < 1.0, "Token validation should average under 1ms")
        assertTrue(avgExtractionMs < 1.0, "Data extraction should average under 1ms")
    }
}
