package at.mocode.ping.feature.presentation

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingResponse
import at.mocode.ping.feature.test.FakePingSyncService
import at.mocode.ping.feature.test.TestPingApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PingViewModelTest {

  private lateinit var viewModel: PingViewModel
  private lateinit var testApiClient: TestPingApiClient
  private lateinit var fakeSyncService: FakePingSyncService

  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    testApiClient = TestPingApiClient()
    fakeSyncService = FakePingSyncService()

    viewModel = PingViewModel(
      apiClient = testApiClient,
      syncService = fakeSyncService
    )
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
    testApiClient.reset()
  }

  @Test
  fun `initial state should be empty`() {
    // Given & When - initial state
    val initialState = viewModel.uiState

    // Then
    assertFalse(initialState.isLoading)
    assertNull(initialState.simplePingResponse)
    assertNull(initialState.enhancedPingResponse)
    assertNull(initialState.healthResponse)
    assertNull(initialState.errorMessage)
  }

  @Test
  fun `performSimplePing should update state with success response`() = runTest(testDispatcher) {
    // Given
    val expectedResponse = PingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service"
    )
    testApiClient.simplePingResponse = expectedResponse

    // When
    viewModel.performSimplePing()
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertEquals(expectedResponse, finalState.simplePingResponse)
    assertNull(finalState.errorMessage)
    assertTrue(testApiClient.simplePingCalled)
  }

  @Test
  fun `performSimplePing should set loading state during execution`() = runTest(testDispatcher) {
    // Given
    testApiClient.simulateDelay = true
    testApiClient.delayMs = 100

    // When
    viewModel.performSimplePing()
    testDispatcher.scheduler.advanceTimeBy(1) // Allow the coroutine to start

    // Then - should be loading during execution
    assertTrue(viewModel.uiState.isLoading)
    assertNull(viewModel.uiState.errorMessage)

    // When - complete the operation
    advanceUntilIdle()

    // Then - should not be loading anymore
    assertFalse(viewModel.uiState.isLoading)
  }

  @Test
  fun `performSimplePing should handle error and update state`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Network error"
    testApiClient.shouldThrowException = true
    testApiClient.exceptionMessage = errorMessage

    // When
    viewModel.performSimplePing()
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertNull(finalState.simplePingResponse)
    assertEquals("Simple ping failed: $errorMessage", finalState.errorMessage)
    assertTrue(testApiClient.simplePingCalled)
  }

  @Test
  fun `performEnhancedPing should update state with success response`() = runTest(testDispatcher) {
    // Given
    val expectedResponse = EnhancedPingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service",
      circuitBreakerState = "CLOSED",
      responseTime = 42L
    )
    testApiClient.enhancedPingResponse = expectedResponse

    // When
    viewModel.performEnhancedPing(simulate = false)
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertEquals(expectedResponse, finalState.enhancedPingResponse)
    assertNull(finalState.errorMessage)
    assertEquals(false, testApiClient.enhancedPingCalledWith)
  }

  @Test
  fun `performEnhancedPing should handle simulate parameter correctly`() = runTest(testDispatcher) {
    // When
    viewModel.performEnhancedPing(simulate = true)
    advanceUntilIdle()

    // Then
    assertEquals(true, testApiClient.enhancedPingCalledWith)
  }

  @Test
  fun `performEnhancedPing should handle error and update state`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Enhanced ping error"
    testApiClient.shouldThrowException = true
    testApiClient.exceptionMessage = errorMessage

    // When
    viewModel.performEnhancedPing()
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertNull(finalState.enhancedPingResponse)
    assertEquals("Enhanced ping failed: $errorMessage", finalState.errorMessage)
  }

  @Test
  fun `performHealthCheck should update state with success response`() = runTest(testDispatcher) {
    // Given
    val expectedResponse = HealthResponse(
      status = "UP",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service",
      healthy = true
    )
    testApiClient.healthResponse = expectedResponse

    // When
    viewModel.performHealthCheck()
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertEquals(expectedResponse, finalState.healthResponse)
    assertNull(finalState.errorMessage)
    assertTrue(testApiClient.healthCheckCalled)
  }

  @Test
  fun `performHealthCheck should handle error and update state`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Health check error"
    testApiClient.shouldThrowException = true
    testApiClient.exceptionMessage = errorMessage

    // When
    viewModel.performHealthCheck()
    advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertNull(finalState.healthResponse)
    assertEquals("Health check failed: $errorMessage", finalState.errorMessage)
  }

  @Test
  fun `triggerSync should call syncService and update state`() = runTest(testDispatcher) {
    // When
    viewModel.triggerSync()
    advanceUntilIdle()

    // Then
    assertTrue(fakeSyncService.syncPingsCalled)
    assertFalse(viewModel.uiState.isSyncing)
    assertNotNull(viewModel.uiState.lastSyncResult)
    assertNull(viewModel.uiState.errorMessage)
  }

  @Test
  fun `triggerSync should handle error and update state`() = runTest(testDispatcher) {
    // Given
    fakeSyncService.shouldThrowException = true
    fakeSyncService.exceptionMessage = "Sync failed"

    // When
    viewModel.triggerSync()
    advanceUntilIdle()

    // Then
    assertTrue(fakeSyncService.syncPingsCalled)
    assertFalse(viewModel.uiState.isSyncing)
    assertEquals("Sync failed: Sync failed", viewModel.uiState.errorMessage)
  }

  @Test
  fun `clearError should remove error message from state`() {
    // Given - set up an error state by simulating an error
    testApiClient.shouldThrowException = true
    runTest(testDispatcher) {
      viewModel.performSimplePing()
      advanceUntilIdle()
    }

    // Verify error is present
    assertNotNull(viewModel.uiState.errorMessage)

    // When
    viewModel.clearError()

    // Then
    assertNull(viewModel.uiState.errorMessage)
    assertFalse(viewModel.uiState.isLoading)
  }

  @Test
  fun `multiple operations should clear previous error messages`() = runTest(testDispatcher) {
    // Given - first operation fails
    testApiClient.shouldThrowException = true
    viewModel.performSimplePing()
    advanceUntilIdle()
    assertNotNull(viewModel.uiState.errorMessage)

    // When - second operation succeeds
    testApiClient.shouldThrowException = false
    val successResponse = PingResponse("SUCCESS", "2025-09-27T21:27:00Z", "test-service")
    testApiClient.simplePingResponse = successResponse
    viewModel.performSimplePing()
    advanceUntilIdle()

    // Then - error should be cleared
    assertNull(viewModel.uiState.errorMessage)
    assertEquals(successResponse, viewModel.uiState.simplePingResponse)
  }

  @Test
  fun `loading state should be false after successful operation`() = runTest(testDispatcher) {
    // Given
    viewModel.performSimplePing()
    advanceUntilIdle()

    // Then
    assertFalse(viewModel.uiState.isLoading)
  }

  @Test
  fun `all operations should call respective API methods`() = runTest(testDispatcher) {
    // When
    viewModel.performSimplePing()
    viewModel.performEnhancedPing(true)
    viewModel.performHealthCheck()
    advanceUntilIdle()

    // Then
    assertTrue(testApiClient.simplePingCalled)
    assertEquals(true, testApiClient.enhancedPingCalledWith)
    assertTrue(testApiClient.healthCheckCalled)
    assertEquals(3, testApiClient.callCount)
  }
}
