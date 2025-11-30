package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PingViewModelTest {

  private lateinit var viewModel: PingViewModel
  private lateinit var testApiClient: TestPingApiClient
  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    testApiClient = TestPingApiClient()
    viewModel = PingViewModel(testApiClient)
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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

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
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val finalState = viewModel.uiState
    assertFalse(finalState.isLoading)
    assertNull(finalState.healthResponse)
    assertEquals("Health check failed: $errorMessage", finalState.errorMessage)
  }

  @Test
  fun `clearError should remove error message from state`() {
    // Given - set up an error state by simulating an error
    testApiClient.shouldThrowException = true
    runTest(testDispatcher) {
      viewModel.performSimplePing()
      testDispatcher.scheduler.advanceUntilIdle()
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
    testDispatcher.scheduler.advanceUntilIdle()
    assertNotNull(viewModel.uiState.errorMessage)

    // When - second operation succeeds
    testApiClient.shouldThrowException = false
    val successResponse = PingResponse("SUCCESS", "2025-09-27T21:27:00Z", "test-service")
    testApiClient.simplePingResponse = successResponse
    viewModel.performSimplePing()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then - error should be cleared
    assertNull(viewModel.uiState.errorMessage)
    assertEquals(successResponse, viewModel.uiState.simplePingResponse)
  }

  @Test
  fun `loading state should be false after successful operation`() = runTest(testDispatcher) {
    // Given
    viewModel.performSimplePing()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertFalse(viewModel.uiState.isLoading)
  }

  @Test
  fun `all operations should call respective API methods`() = runTest(testDispatcher) {
    // When
    viewModel.performSimplePing()
    viewModel.performEnhancedPing(true)
    viewModel.performHealthCheck()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(testApiClient.simplePingCalled)
    assertEquals(true, testApiClient.enhancedPingCalledWith)
    assertTrue(testApiClient.healthCheckCalled)
    assertEquals(3, testApiClient.callCount)
  }
}
