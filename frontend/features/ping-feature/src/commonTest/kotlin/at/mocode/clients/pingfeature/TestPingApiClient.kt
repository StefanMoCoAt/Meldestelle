package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingEvent

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
  var publicPingResponse: PingResponse? = null
  var securePingResponse: PingResponse? = null
  var syncPingsResponse: List<PingEvent> = emptyList()

  // Call tracking
  var simplePingCalled = false
  var enhancedPingCalledWith: Boolean? = null
  var healthCheckCalled = false
  var publicPingCalled = false
  var securePingCalled = false
  var syncPingsCalledWith: Long? = null
  var callCount = 0

  override suspend fun simplePing(): PingResponse {
    simplePingCalled = true
    callCount++
    return handleRequest(simplePingResponse)
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

  override suspend fun publicPing(): PingResponse {
    publicPingCalled = true
    callCount++
    return handleRequest(publicPingResponse)
  }

  override suspend fun securePing(): PingResponse {
    securePingCalled = true
    callCount++
    return handleRequest(securePingResponse)
  }

  override suspend fun syncPings(lastSyncTimestamp: Long): List<PingEvent> {
    syncPingsCalledWith = lastSyncTimestamp
    callCount++

    if (simulateDelay) {
      kotlinx.coroutines.delay(delayMs)
    }

    if (shouldThrowException) {
      throw Exception(exceptionMessage)
    }

    return syncPingsResponse
  }

  private suspend fun handleRequest(response: PingResponse?): PingResponse {
    if (simulateDelay) {
      kotlinx.coroutines.delay(delayMs)
    }

    if (shouldThrowException) {
      throw Exception(exceptionMessage)
    }

    return response ?: PingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-ping-service"
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
    publicPingResponse = null
    securePingResponse = null
    syncPingsResponse = emptyList()
    simplePingCalled = false
    enhancedPingCalledWith = null
    healthCheckCalled = false
    publicPingCalled = false
    securePingCalled = false
    syncPingsCalledWith = null
    callCount = 0
  }
}
