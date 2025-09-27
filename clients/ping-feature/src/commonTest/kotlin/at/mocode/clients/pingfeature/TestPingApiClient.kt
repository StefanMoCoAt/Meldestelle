package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse

/**
 * Test double implementation of PingApi for testing purposes.
 * This allows us to test ViewModel behavior without needing MockK.
 */
class TestPingApiClient : PingApi {

    // Test configuration properties
    var shouldThrowException = false
    var exceptionMessage = "Test exception"
    var simulateDelay = false
    var delayMs = 100L

    // Response configuration
    var simplePingResponse: PingResponse? = null
    var enhancedPingResponse: EnhancedPingResponse? = null
    var healthResponse: HealthResponse? = null

    // Call tracking
    var simplePingCalled = false
    var enhancedPingCalledWith: Boolean? = null
    var healthCheckCalled = false
    var callCount = 0

    override suspend fun simplePing(): PingResponse {
        simplePingCalled = true
        callCount++

        if (simulateDelay) {
            kotlinx.coroutines.delay(delayMs)
        }

        if (shouldThrowException) {
            throw Exception(exceptionMessage)
        }

        return simplePingResponse ?: PingResponse(
            status = "OK",
            timestamp = "2025-09-27T21:27:00Z",
            service = "test-ping-service"
        )
    }

    override suspend fun enhancedPing(simulate: Boolean): EnhancedPingResponse {
        enhancedPingCalledWith = simulate
        callCount++

        if (simulateDelay) {
            kotlinx.coroutines.delay(delayMs)
        }

        if (shouldThrowException) {
            throw Exception(exceptionMessage)
        }

        return enhancedPingResponse ?: EnhancedPingResponse(
            status = "OK",
            timestamp = "2025-09-27T21:27:00Z",
            service = "test-ping-service",
            circuitBreakerState = "CLOSED",
            responseTime = 42L
        )
    }

    override suspend fun healthCheck(): HealthResponse {
        healthCheckCalled = true
        callCount++

        if (simulateDelay) {
            kotlinx.coroutines.delay(delayMs)
        }

        if (shouldThrowException) {
            throw Exception(exceptionMessage)
        }

        return healthResponse ?: HealthResponse(
            status = "UP",
            timestamp = "2025-09-27T21:27:00Z",
            service = "test-ping-service",
            healthy = true
        )
    }

    // Test utilities
    fun reset() {
        shouldThrowException = false
        exceptionMessage = "Test exception"
        simulateDelay = false
        delayMs = 100L
        simplePingResponse = null
        enhancedPingResponse = null
        healthResponse = null
        simplePingCalled = false
        enhancedPingCalledWith = null
        healthCheckCalled = false
        callCount = 0
    }
}
